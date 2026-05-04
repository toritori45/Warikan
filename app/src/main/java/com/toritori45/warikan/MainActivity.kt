package com.example.warikan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

data class PersonResult(val name: String, val amount: Long)
data class HistoryEntry(val title: String, val totalAmount: Long, val count: Int, val perPerson: Long)

class NameInputAdapter(
    private val names: MutableList<String>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<NameInputAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val etName: EditText = view.findViewById(R.id.etPersonName)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemovePerson)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_name_input, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.etName.setText(names[position])
        holder.etName.setOnFocusChangeListener { _, _ ->
            names[holder.adapterPosition] = holder.etName.text.toString()
        }
        holder.btnRemove.setOnClickListener {
            val pos = holder.adapterPosition
            names[pos] = holder.etName.text.toString()
            onRemove(pos)
        }
    }

    override fun getItemCount() = names.size
}

class PersonResultAdapter(private val results: List<PersonResult>) :
    RecyclerView.Adapter<PersonResultAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPersonName)
        val tvAmount: TextView = view.findViewById(R.id.tvPersonAmount)
        val tvBadge: TextView = view.findViewById(R.id.tvPersonBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = results[position]
        holder.tvName.text = item.name
        holder.tvAmount.text = formatYen(item.amount)
        holder.tvBadge.text = (position + 1).toString()
    }

    override fun getItemCount() = results.size
}

class HistoryAdapter(private val history: List<HistoryEntry>) :
    RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvHistoryTitle: TextView = view.findViewById(R.id.tvHistoryTitle)
        val tvHistoryTotal: TextView = view.findViewById(R.id.tvHistoryTotal)
        val tvHistoryPer: TextView = view.findViewById(R.id.tvHistoryPer)
        val tvHistoryCount: TextView = view.findViewById(R.id.tvHistoryCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = history[position]
        holder.tvHistoryTitle.text = item.title
        holder.tvHistoryTotal.text = "合計: ${formatYen(item.totalAmount)}"
        holder.tvHistoryPer.text = "一人: ${formatYen(item.perPerson)}"
        holder.tvHistoryCount.text = "${item.count}人"
    }

    override fun getItemCount() = history.size
}

private fun formatYen(amount: Long): String {
    val fmt = NumberFormat.getNumberInstance(Locale.JAPAN)
    return "¥${fmt.format(amount)}"
}

class MainActivity : AppCompatActivity() {

    private val nameList = mutableListOf("", "")
    private lateinit var nameAdapter: NameInputAdapter
    private val resultList = mutableListOf<PersonResult>()
    private lateinit var resultAdapter: PersonResultAdapter
    private val historyList = mutableListOf<HistoryEntry>()
    private lateinit var historyAdapter: HistoryAdapter

    // Views
    private lateinit var rvNames: RecyclerView
    private lateinit var rvResults: RecyclerView
    private lateinit var rvHistory: RecyclerView
    private lateinit var etTotalAmount: EditText
    private lateinit var tvResultSummary: TextView
    private lateinit var tvPerPerson: TextView
    private lateinit var layoutCalc: LinearLayout
    private lateinit var layoutHistory: LinearLayout
    private lateinit var layoutResult: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvNames        = findViewById(R.id.rvNames)
        rvResults      = findViewById(R.id.rvResults)
        rvHistory      = findViewById(R.id.rvHistory)
        etTotalAmount  = findViewById(R.id.etTotalAmount)
        tvResultSummary= findViewById(R.id.tvResultSummary)
        tvPerPerson    = findViewById(R.id.tvPerPerson)
        layoutCalc     = findViewById(R.id.layoutCalc)
        layoutHistory  = findViewById(R.id.layoutHistory)
        layoutResult   = findViewById(R.id.layoutResult)

        setupNameRecycler()
        setupResultRecycler()
        setupHistoryRecycler()
        setupButtons()
        showTab(0)
    }

    private fun setupNameRecycler() {
        nameAdapter = NameInputAdapter(nameList) { pos ->
            if (nameList.size > 2) {
                nameList.removeAt(pos)
                nameAdapter.notifyItemRemoved(pos)
                nameAdapter.notifyItemRangeChanged(pos, nameList.size)
            } else {
                Toast.makeText(this, "最低2人必要です", Toast.LENGTH_SHORT).show()
            }
        }
        rvNames.layoutManager = LinearLayoutManager(this)
        rvNames.adapter = nameAdapter
        rvNames.isNestedScrollingEnabled = false
    }

    private fun setupResultRecycler() {
        resultAdapter = PersonResultAdapter(resultList)
        rvResults.layoutManager = LinearLayoutManager(this)
        rvResults.adapter = resultAdapter
        rvResults.isNestedScrollingEnabled = false
    }

    private fun setupHistoryRecycler() {
        historyAdapter = HistoryAdapter(historyList)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = historyAdapter
        rvHistory.isNestedScrollingEnabled = false
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnAddPerson).setOnClickListener {
            nameList.add("")
            nameAdapter.notifyItemInserted(nameList.size - 1)
        }
        findViewById<Button>(R.id.btnCalculate).setOnClickListener {
            calculate()
        }
        findViewById<Button>(R.id.btnShareLine).setOnClickListener {
            shareResult()
        }
        findViewById<Button>(R.id.btnTabCalc).setOnClickListener { showTab(0) }
        findViewById<Button>(R.id.btnTabHistory).setOnClickListener { showTab(1) }
    }

    private fun showTab(tab: Int) {
        if (tab == 0) {
            layoutCalc.visibility    = View.VISIBLE
            layoutHistory.visibility = View.GONE
        } else {
            layoutCalc.visibility    = View.GONE
            layoutHistory.visibility = View.VISIBLE
        }
    }

    private fun collectNames(): List<String> {
        val collected = mutableListOf<String>()
        for (i in 0 until rvNames.childCount) {
            val child = rvNames.getChildAt(i)
            val et = child?.findViewById<EditText>(R.id.etPersonName)
            val name = et?.text?.toString()?.trim() ?: ""
            if (name.isNotEmpty()) collected.add(name)
        }
        return collected
    }

    private fun calculate() {
        val totalStr = etTotalAmount.text.toString().trim()
        if (totalStr.isEmpty()) {
            Toast.makeText(this, "合計金額を入力してください", Toast.LENGTH_SHORT).show()
            return
        }
        val total = totalStr.toLongOrNull()
        if (total == null || total <= 0) {
            Toast.makeText(this, "正しい金額を入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        val names = collectNames()
        if (names.size < 2) {
            Toast.makeText(this, "2人以上の名前を入力してください", Toast.LENGTH_SHORT).show()
            return
        }

        val base      = total / names.size
        val remainder = total % names.size

        resultList.clear()
        names.forEachIndexed { index, name ->
            val amount = if (index < remainder) base + 1 else base
            resultList.add(PersonResult(name, amount))
        }
        resultAdapter.notifyDataSetChanged()

        tvResultSummary.text = "${names.size}人で ${formatYen(total)} を割り勘"
        tvPerPerson.text     = "${formatYen(base)}〜${formatYen(base + 1)}"
        layoutResult.visibility = View.VISIBLE

        historyList.add(0, HistoryEntry(
            title       = names.joinToString("・"),
            totalAmount = total,
            count       = names.size,
            perPerson   = base
        ))
        historyAdapter.notifyItemInserted(0)
    }

    private fun shareResult() {
        if (resultList.isEmpty()) {
            Toast.makeText(this, "先に割り勘を計算してください", Toast.LENGTH_SHORT).show()
            return
        }

        val sb = StringBuilder()
        sb.appendLine("【かんたん割り勘電卓】")
        sb.appendLine(tvResultSummary.text)
        sb.appendLine()
        resultList.forEach { sb.appendLine("${it.name}：${formatYen(it.amount)}") }
        sb.appendLine()
        sb.append("✅ かんたん割り勘電卓で計算")

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }

        // LINEがインストールされていれば直接開く、なければ共有シートを表示
        shareIntent.setPackage("jp.naver.line.android")
        val lineAvailable = packageManager.queryIntentActivities(shareIntent, 0).isNotEmpty()
        if (!lineAvailable) shareIntent.setPackage(null)

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_chooser_title)))
    }
}
