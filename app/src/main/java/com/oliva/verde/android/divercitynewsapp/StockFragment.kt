package com.oliva.verde.android.divercitynewsapp

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A simple [Fragment] subclass.
 */
class StockFragment : Fragment() {
    var articleList = mutableListOf<Article>()
    var longClickedId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stock, container, false)
        val cursor = selectAllArticle()
        var title = ""
        var publishedAt = ""
        var urlToImage = ""
        var url = ""

        while(cursor.moveToNext()) {
            val idxTitle = cursor.getColumnIndex("title")
            title = cursor.getString(idxTitle)
            val idxPublishedAt = cursor.getColumnIndex("published_at")
            publishedAt = cursor.getString(idxPublishedAt)
            val idxUrlToImage = cursor.getColumnIndex("url_to_image")
            urlToImage = cursor.getString(idxUrlToImage)
            val idxUrl = cursor.getColumnIndex("url")
            url = cursor.getString(idxUrl)
            articleList.add(Article(url, urlToImage, publishedAt, title))
        }
        val lvArticles = view.findViewById<RecyclerView>(R.id.lvArticles)
        val layout = LinearLayoutManager(activity)
        lvArticles.layoutManager = layout
        lvArticles.adapter = RecycleListAdapter(this@StockFragment, articleList)
        // リサイクラービューに区切り線を追加
        val decorator = DividerItemDecoration(activity, layout.orientation)
        lvArticles?.addItemDecoration(decorator)

        return view
    }

    override fun onDestroy() {
        val helper = DataBaseHelper(activity!!)
        helper.close()
        super.onDestroy()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val menuInflater = MenuInflater(activity)
        menuInflater.inflate(R.menu.context_menu_remove_from_stock, menu)
        menu.setHeaderTitle(R.string.news_list_context_header)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        // データベースから記事データのIDを全て取得する
        val cursor = selectAllArticle()
        val idArray = arrayListOf<Long>()
        while(cursor.moveToNext()) {
            val idxId = cursor.getColumnIndex("_id")
            idArray.add(cursor.getLong(idxId))
        }
        // 長押しされた記事のデータベース上のidを取得
        val selectedArticleId = idArray[longClickedId]
        // 長押しされた記事をデータベース上から消去する
        val sqlDelete = "DELETE FROM stocked_articles WHERE _id = ?"
        val stmt = DataBaseHelper(activity!!).writableDatabase.compileStatement(sqlDelete)
        stmt.bindLong(1, selectedArticleId)
        stmt.executeUpdateDelete()

        // 長押しされた記事オブジェクトをリサイクラービューから削除する
        val lvArticles = view?.findViewById<RecyclerView>(R.id.lvArticles)
        val article = articleList[longClickedId] // 長押しされた記事オブジェクトをリストビューから取得
        val adapter = lvArticles?.adapter as RecycleListAdapter // リサイクラービューに設定されているアダプターを取得
        // 記事オブジェクト配列から記事オブジェクトを削除
        articleList.remove(article)
        // アダプターに、アダプト対象の記事オブジェクトの変更を知らせる
        adapter.notifyDataSetChanged()
        Toast.makeText(activity, R.string.success_to_remove_from_stock, Toast.LENGTH_LONG).show()

        return super.onContextItemSelected(item)
    }

    inner class ListItemClickListener(position: Int) : View.OnClickListener {
        val position = position
        override fun onClick(view: View?) {
            val item = articleList[position]
            // url文字列を取得
            val url = item.url
            //以下、Custom Tabs機能を使って記事の詳細を表示する
            // Custom Tabを表示するBuilderオブジェクトを取得
            val builder = CustomTabsIntent.Builder()
            // CustomTabsIntentオブジェクトを取得
            val customTabsIntent = builder.build()
            // Uriを指定し、Custom Tabを表示する
            customTabsIntent.launchUrl(activity!!, Uri.parse(url))
        }
    }

    inner class ListItemLongClickListener(position: Int) : View.OnLongClickListener {
        val pos = position
        override fun onLongClick(v: View?): Boolean {
            longClickedId = pos
            return false
        }
    }

    private fun selectAllArticle() : Cursor {
        val helper = DataBaseHelper(activity!!)
        val db = helper.writableDatabase
        val sql = "SELECT * FROM stocked_articles"
        return db.rawQuery(sql, null)
    }
}
