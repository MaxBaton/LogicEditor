package com.example.logiceditor.activity

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.logiceditor.R
import com.example.logiceditor.databinding.ActivityWelcomeBinding
import com.example.logiceditor.databinding.CircuitElementBinding
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.io.File

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private val groupAdapterCircuit = GroupAdapter<GroupieViewHolder>()
    private var listNameCircuit = listOf<String>()

    companion object {
        const val NAME_CIRCUIT = "nameCircuit"
        const val DATE_CIRCUIT = "dateCircuit"
        var isChangeListCircuit = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        updateActionBar()
        StatusBar.updateStatusBar(this)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)

            recyclerViewCircuit.apply {
                layoutManager = GridLayoutManager(this@WelcomeActivity, 3)
                adapter = groupAdapterCircuit
                addItemDecoration(RecyclerViewCircuitItemDecoration(3))
            }
        }

        listNameCircuit = getNamesCircuit()!!.toList().sorted()

        groupAdapterCircuit.setOnItemClickListener { item, _ ->
            val circuit = item as CircuitItem
            val date = circuit.date.replace("/","-")
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(NAME_CIRCUIT, circuit.name)
            intent.putExtra(DATE_CIRCUIT, date)
            startActivity(intent)
        }

        groupAdapterCircuit.setOnItemLongClickListener { item, view ->
            val circuitItem = item as CircuitItem
            if (circuitItem.index != 0) {
                val name = circuitItem.name
                val date = circuitItem.date.replace("/","-")
                val popupMenu = createPopupMenu(R.menu.menu_circuit_option, view)
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId) {
                        R.id.circuitElementRename -> {
                            val fileName = "${name}_DATE_${date}"
                            createRenameAlertDialog(this, dir = applicationContext.filesDir.absolutePath, fileName = fileName).show()
                            true
                        }
                        R.id.circuitElementDelete -> {
                            val fileName = "${name}_DATE_${date}"
                            createConfirmDeleteCircuitAlertDialog(this, fileName).show()
                            true
                        }
                        else -> false
                    }
                }
                popupMenu.show()
                true
            }else false
        }

    }

    override fun onStop() {
        super.onStop()

        isChangeListCircuit = false
    }

    override fun onStart() {
        super.onStart()

        if (isChangeListCircuit) {
            clearGroupAdapter()
        }

        if (groupAdapterCircuit.groupCount == 0) {
            fillWithDataRecyclerView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_welcome_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when(item.itemId) {
                R.id.referenceItem -> {
                    createReferenceWelcomeActivityAlertDialog().show()
                    true
                }
            else ->false
        }

    private fun updateActionBar() {
        this.supportActionBar!!.title = getString(R.string.app_name)
        this.supportActionBar!!.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.background_circuit_color_round)))
    }

    fun updateRecyclerView() {
        clearGroupAdapter()
        fillWithDataRecyclerView()
    }

    private fun clearGroupAdapter() {
        listNameCircuit = getNamesCircuit()!!.toList().sorted()
        groupAdapterCircuit.clear()
    }

    private fun fillWithDataRecyclerView() {
        groupAdapterCircuit.add(CircuitItem(R.drawable.new_circuit_icon, getString(R.string.new_circuit), "", 0))

        listNameCircuit.forEachIndexed { index, nameCircuit ->
            val name = nameCircuit.substringBefore(MainActivity.DATE_DELIMITER)
            val date = nameCircuit.substringAfter(MainActivity.DATE_DELIMITER).replace("-","/")
            groupAdapterCircuit.add(CircuitItem(R.drawable.circuit_icon, name, date, index + 1))
        }
    }

    private fun getNamesCircuit() = applicationContext.filesDir.list()

    fun deleteCircuit(fileName: String) {
        val dir = applicationContext.filesDir.absolutePath
        val sourceFile = File(dir,fileName)
        sourceFile.delete()
    }

    inner class CircuitItem(private val image: Int, val name: String, val date: String, var index: Int): Item<GroupieViewHolder>()  {

        override fun getLayout() = R.layout.circuit_element

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val _binding = CircuitElementBinding.bind(viewHolder.itemView)

            Glide
                .with(this@WelcomeActivity)
                .load(image)
                .into(_binding.imageViewLoadElement)

            _binding.textViewNameCircuit2.text = name
            _binding.textViewDateCircuit2.text = date
        }
    }
}