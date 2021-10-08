package com.example.logiceditor.activity

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isEmpty
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.example.logiceditor.R
import com.example.logiceditor.databinding.ActionElementBinding
import com.example.logiceditor.databinding.ActivityMainBinding
import com.example.logiceditor.databinding.LogicElementBinding
import com.example.logiceditor.tools.elements.Tool
import com.example.logiceditor.tools.wire.Wire
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private val groupAdapterElement = GroupAdapter<GroupieViewHolder>()
    private val groupAdapterAction = GroupAdapter<GroupieViewHolder>()
    private val arrayIsClickActionItem = Array(ListsRecyclerView.listIconAction.size - 1) {false}
    private var toolWireCount = -1 to -1

    companion object{
        lateinit var _resources: Resources
        var nameDate = "" to ""
        const val DATE_DELIMITER = "_DATE_"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StatusBar.updateStatusBar(this)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val nameCircuit = intent.getStringExtra(WelcomeActivity.NAME_CIRCUIT)
        val dateCircuit = intent.getStringExtra(WelcomeActivity.DATE_CIRCUIT)
        if (isLoadCircuit(nameCircuit!!, dateCircuit!!)) {
            val fileName = "${nameCircuit}_DATE_${dateCircuit}"
            loadCircuit(fileName)
            nameDate = nameCircuit to dateCircuit
            toolWireCount = binding.sketcherView.game.usedTools.size to binding.sketcherView.game.savedWire.size
            binding.sketcherView.enableDrawing()
        }else nameDate = "" to ""

        with(binding) {
            setContentView(root)

            _resources = sketcherView.resources

            recyclerViewElements.adapter = groupAdapterElement
            recyclerViewElements.addItemDecoration(
                DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL)
            )

            recyclerViewAction.adapter = groupAdapterAction
            recyclerViewAction.addItemDecoration(
                DividerItemDecoration(this@MainActivity, DividerItemDecoration.HORIZONTAL)
            )
        }

        groupAdapterElement.setOnItemClickListener { item, _ ->
            val element = item as ElementItem
            clickElement(element)
        }

        groupAdapterAction.setOnItemClickListener { item, _ ->
            val action = item as ActionItem
            changeActionElement(action)
        }
    }

    override fun onStart() {
        super.onStart()

        if (binding.recyclerViewElements.isEmpty()) {
                ListsRecyclerView.listNameElements.forEachIndexed { index, name ->
                val temp = BitmapFactory.decodeResource(
                    binding.sketcherView.resources,
                    ListsRecyclerView.listIconElements[index]
                )
                val bitmap = Bitmap.createScaledBitmap(temp, 150, 80, true)
                groupAdapterElement.add(ElementItem(name, bitmap))
            }
        }

        if (binding.recyclerViewAction.isEmpty()) {
            ListsRecyclerView.listIconAction.forEachIndexed { index, icon ->
                    groupAdapterAction.add(ActionItem(icon, index))
            }
        }
    }

    private fun isLoadCircuit(name: String, date: String) = !(name == getString(R.string.new_circuit) && date == "")

    private fun clickElement(element: ElementItem) {
        if (arrayIsClickActionItem[ListsRecyclerView.listIconAction.indexOf(R.drawable.start)]) {
            Toast.makeText(this, getString(R.string.toast_turn_off_simulation), Toast.LENGTH_SHORT).show()
            return
        }

        arrayIsClickActionItem.forEachIndexed { index, value ->
            if ((index == (ListsRecyclerView.listIconAction.indexOf(R.drawable.wire)) || index ==
                        (ListsRecyclerView.listIconAction.indexOf(R.drawable.delete)) ||
                            index == (ListsRecyclerView.listIconAction.indexOf(R.drawable.inversion))) && value) {
                if (arrayIsClickActionItem[index]) {
                    val actionItem = groupAdapterAction.getItem(index) as ActionItem
                    arrayIsClickActionItem[index] = false
                    actionItem.isBtnClick = false
                    groupAdapterAction.notifyItemChanged(index)
                }
            }
        }

        binding.sketcherView.game.isDelete = false

        when (element.name) {
            getString(R.string.AND_name) -> {
                binding.sketcherView.game.addAND(R.drawable.and_element)
                binding.sketcherView.drawingThread.run()
                binding.sketcherView.enableDrawing()
            }
            getString(R.string.OR_name) -> {
                binding.sketcherView.game.addOR(R.drawable.or_element)
                binding.sketcherView.drawingThread.run()
                binding.sketcherView.enableDrawing()
            }
            getString(R.string.NOT_NAME) -> {
                binding.sketcherView.game.addNOT(R.drawable.not_elemet)
                binding.sketcherView.drawingThread.run()
                binding.sketcherView.enableDrawing()
            }
            getString(R.string.XOR_name) -> {
                binding.sketcherView.game.addXOR(R.drawable.xor_element)
                binding.sketcherView.drawingThread.run()
                binding.sketcherView.enableDrawing()
            }
            getString(R.string.Button_name) -> {
                binding.sketcherView.game.addButton(R.drawable.button_off_element)
                binding.sketcherView.drawingThread.run()
                binding.sketcherView.enableDrawing()
            }
            getString(R.string.LED_name) -> {
                binding.sketcherView.game.addLED(R.drawable.light_element)
                binding.sketcherView.drawingThread.run()
                binding.sketcherView.enableDrawing()
            }
        }
    }

    private fun clickAction(action: ActionItem) =
        when(action.icon) {
            R.drawable.wire -> {
                val indexWire = action.index
                val inOut = if (arrayIsClickActionItem[indexWire]) getString(R.string.str_in) else getString(R.string.str_out)
                if (arrayIsClickActionItem[indexWire]) binding.sketcherView.enableWiring() else
                                                                                    binding.sketcherView.enableDrawing()
                Toast.makeText(this, "Соединение - ${inOut}", Toast.LENGTH_SHORT).show()
                true
            }
            R.drawable.start -> {
                if (!binding.sketcherView.game.checkOutputs()) {
                    Toast.makeText(this@MainActivity, getString(R.string.toast_connect_all_outs), Toast.LENGTH_SHORT).show()
                    false
                } else if (!binding.sketcherView.game.checkConnections()) {
                    Toast.makeText(this@MainActivity, getString(R.string.wrong_count_connections), Toast.LENGTH_SHORT).show()
                    false
                }else if (!binding.sketcherView.game.isLEDOne()) {
                    Toast.makeText(this@MainActivity, getString(R.string.toast_LED_needed), Toast.LENGTH_SHORT).show()
                    false
                }else {
                    if (!binding.sketcherView.game.isSimulate) {
                        binding.sketcherView.enableDrawing()
                        binding.sketcherView.game.isSimulate = true
                        binding.sketcherView.game.simulate()
                        binding.sketcherView.drawingThread.run()
                    }else {
                        val indexWire = ListsRecyclerView.listIconAction.indexOf(R.drawable.wire)
                        binding.sketcherView.game.isSimulate = false
                        if (arrayIsClickActionItem[indexWire]) binding.sketcherView.enableWiring() else
                                                                    binding.sketcherView.enableDrawing()
                    }
                    true
                }
            }
            R.drawable.delete -> {
                val indexDelete = action.index
                val inOut = if (arrayIsClickActionItem[indexDelete]) getString(R.string.str_in) else getString(R.string.str_out)
                if (arrayIsClickActionItem[indexDelete]) {
                    binding.sketcherView.enableDeleting()
                } else {
                    binding.sketcherView.enableDrawing()
                }
                Toast.makeText(this, "Удаление - $inOut", Toast.LENGTH_SHORT).show()
                true
            }
            R.drawable.save -> {
                createSaveAlertDialog(this@MainActivity).show()
                true
            }
            R.drawable.reference -> {
                createReferenceAlertDialog().show()
                true
            }
            R.drawable.exit -> {
                if (toolWireCount.first == -1) {
                    toolWireCount = binding.sketcherView.game.usedTools.size to
                                                                binding.sketcherView.game.savedWire.size
                    if (toolWireCount.first != 0) {
                        createNotSaveCircuitAlertDialog(this).show()
                    }else {
                        this.finishAndRemoveTask()
                    }
                }else {
                    val currToolWireCount = binding.sketcherView.game.usedTools.size to
                                                                binding.sketcherView.game.savedWire.size
                    if (toolWireCount != currToolWireCount) {
                        createNotSaveCircuitAlertDialog(this).show()
                    }else {
                        this.finishAndRemoveTask()
                    }
                }
                true
            }
            R.drawable.inversion -> {
                val indexInversion = action.index
                val inOut = if (arrayIsClickActionItem[indexInversion]) getString(R.string.str_in) else getString(R.string.str_out)
                if (arrayIsClickActionItem[indexInversion]) binding.sketcherView.enableInversion() else
                                                                            binding.sketcherView.enableDrawing()
                Toast.makeText(this, "Инверсия - $inOut", Toast.LENGTH_SHORT).show()
                true
            }
            R.drawable.move -> {
                val indexMoving = action.index
                val inOut = if (arrayIsClickActionItem[indexMoving]) getString(R.string.str_in) else getString(R.string.str_out)
                if (arrayIsClickActionItem[indexMoving]) binding.sketcherView.enableMoving() else
                    binding.sketcherView.enableDrawing()
                toastShort("Перемещение - $inOut")
                true
            }
            else -> false
        }

    private fun changeActionElement(action: ActionItem) {
        val indexStart = ListsRecyclerView.listIconAction.indexOf(R.drawable.start)

        when(action.icon) {
            R.drawable.wire -> {
                if (!arrayIsClickActionItem[indexStart]) {
                    val indexWire = ListsRecyclerView.listIconAction.indexOf(R.drawable.wire)
                    arrayIsClickActionItem[indexWire] = !arrayIsClickActionItem[indexWire]
                    action.isBtnClick = arrayIsClickActionItem[indexWire]
                    groupAdapterAction.notifyItemChanged(action.index)
                    clickAction(action)

                    arrayIsClickActionItem.forEachIndexed { index, value ->
                        if (index != indexWire && value) {
                            val _action = groupAdapterAction.getItem(index) as ActionItem
                            arrayIsClickActionItem[index] = false
                            _action.isBtnClick = false
                            groupAdapterAction.notifyItemChanged(index)
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.toast_turn_off_simulation, Toast.LENGTH_SHORT).show()
                }
            }
            R.drawable.start -> {
                if (clickAction(action)) {
                    arrayIsClickActionItem[indexStart] = !arrayIsClickActionItem[indexStart]
                    action.isBtnClick = arrayIsClickActionItem[indexStart]
                    groupAdapterAction.notifyItemChanged(indexStart)

                    arrayIsClickActionItem.forEachIndexed { index, value ->
                        if (index != indexStart && value) {
                            val actionItem = groupAdapterAction.getItem(index) as ActionItem
                            arrayIsClickActionItem[index] = false
                            actionItem.isBtnClick = false
                            groupAdapterAction.notifyItemChanged(index)
                        }
                    }
                }else arrayIsClickActionItem[indexStart] = false
            }
            R.drawable.delete -> {
                if (!arrayIsClickActionItem[indexStart]) {
                    val indexDelete = ListsRecyclerView.listIconAction.indexOf(R.drawable.delete)
                    arrayIsClickActionItem[indexDelete] = !arrayIsClickActionItem[indexDelete]
                    action.isBtnClick = arrayIsClickActionItem[indexDelete]
                    groupAdapterAction.notifyItemChanged(action.index)
                    clickAction(action)

                    arrayIsClickActionItem.forEachIndexed { index, value ->
                        if (index != indexDelete && value) {
                            val _action = groupAdapterAction.getItem(index) as ActionItem
                            arrayIsClickActionItem[index] = false
                            _action.isBtnClick = false
                            groupAdapterAction.notifyItemChanged(index)
                        }
                    }
                } else {
                    Toast.makeText(this, R.string.toast_turn_off_simulation, Toast.LENGTH_SHORT).show()
                }
            }
           R.drawable.save -> {
               if (!arrayIsClickActionItem[indexStart]){
                   clickAction(action)
               }else {
                   Toast.makeText(this, R.string.toast_turn_off_simulation, Toast.LENGTH_SHORT).show()
               }
           }
           R.drawable.inversion -> {
               if (!arrayIsClickActionItem[indexStart]){
                   val indexInversion = ListsRecyclerView.listIconAction.indexOf(R.drawable.inversion)
                   arrayIsClickActionItem[indexInversion] = !arrayIsClickActionItem[indexInversion]
                   action.isBtnClick = arrayIsClickActionItem[indexInversion]
                   groupAdapterAction.notifyItemChanged(action.index)
                   clickAction(action)

                   arrayIsClickActionItem.forEachIndexed { index, value ->
                       if (index != indexInversion && value) {
                           val _action = groupAdapterAction.getItem(index) as ActionItem
                           arrayIsClickActionItem[index] = false
                           _action.isBtnClick = false
                           groupAdapterAction.notifyItemChanged(index)
                       }
                   }
               }else {
                   Toast.makeText(this, R.string.toast_turn_off_simulation, Toast.LENGTH_SHORT).show()
               }
           }
            R.drawable.move -> {
                if (!arrayIsClickActionItem[indexStart]) {
                    val indexMoving = ListsRecyclerView.listIconAction.indexOf(R.drawable.move)
                    arrayIsClickActionItem[indexMoving] = !arrayIsClickActionItem[indexMoving]
                    action.isBtnClick = arrayIsClickActionItem[indexMoving]
                    groupAdapterAction.notifyItemChanged(action.index)
                    clickAction(action)

                    arrayIsClickActionItem.forEachIndexed { index, value ->
                        if (index != indexMoving && value) {
                            val _action = groupAdapterAction.getItem(index) as ActionItem
                            arrayIsClickActionItem[index] = false
                            _action.isBtnClick = false
                            groupAdapterAction.notifyItemChanged(index)
                        }
                    }
                }else {
                    toastShort(getString(R.string.toast_turn_off_simulation))
                }
            }
            R.drawable.reference -> clickAction(action)
            R.drawable.exit -> clickAction(action)
        }
    }

    fun saveCircuit(name: String, isRewrite: Boolean = false) {
        // save circuit in android file system
        val tools = binding.sketcherView.game.usedTools
        val wires = binding.sketcherView.game.savedWire
        saveInFile(tools, wires, name, isRewrite)
    }

    fun isExistNameCircuit(name: String): String? {
        getNamesCircuit()!!.forEach {
            val _name = it.substringBefore(DATE_DELIMITER)
            val date = it.substringAfter(name)
            if (name == _name) return "${name}${date}"
        }

        return null
    }

    private fun saveInFile(tools: MutableList<Tool?>, wires: MutableList<Wire>, fileName: String,
                            isRewrite: Boolean = false) {
        val name = if (!isRewrite) {
            val date = CurrentDate.getCurrentDate()
            "${fileName}_DATE_${date}"
        }else {
            fileName
        }

        val fos = applicationContext.openFileOutput(name, Context.MODE_PRIVATE)
        val os = ObjectOutputStream(fos)
        os.writeObject(tools)
        os.writeObject(wires)


        if (isRewrite) {
            // change date of file
            renameFile(fileName)
        }

        os.close()
        fos.close()
    }

    private fun renameFile(fileName: String) {
        val dir = applicationContext.filesDir.absolutePath
        val sourceFile = File(dir,fileName)

        val name = fileName.substringBefore(DATE_DELIMITER)
        val date = CurrentDate.getCurrentDate()//getCurrentDate()
        val newFileName = "${name}_DATE_${date}"

        val destFile= File(dir,newFileName)
        sourceFile.renameTo(destFile)
    }

    private fun getNamesCircuit() = applicationContext.filesDir.list()

    private fun loadCircuit(fileName: String) {
        // load circuit
        val fis = applicationContext.openFileInput(fileName)
        val inputStream = ObjectInputStream(fis)
        val tools = inputStream.readObject()!! as MutableList<Tool?>
        val wires = inputStream.readObject()!! as MutableList<Wire>
        binding.sketcherView.game.usedTools =  tools
        binding.sketcherView.game.savedWire =  wires
        inputStream.close()
        fis.close()
    }

    fun updateToolWireCount() {
        toolWireCount = binding.sketcherView.game.usedTools.size to binding.sketcherView.game.savedWire.size
    }

    inner class ElementItem(val name: String, private val icon: Bitmap): Item<GroupieViewHolder>() {

        override fun getLayout() = R.layout.logic_element

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val _binding = LogicElementBinding.bind(viewHolder.itemView)

            _binding.textViewNameElement.text = name

            Glide
                .with(this@MainActivity).asBitmap()
                .load(icon)
                .into(_binding.imageViewElement)
        }
    }

    inner class ActionItem(var icon: Int, val index: Int, var isBtnClick: Boolean = false): Item<GroupieViewHolder>() {

        override fun getLayout() = R.layout.action_element

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            val _binding = ActionElementBinding.bind(viewHolder.itemView)

            if (isBtnClick && (index <= ListsRecyclerView.listIconActionClick.size)) {
                val clickIcon = ListsRecyclerView.listIconActionClick[index]

                Glide
                        .with(this@MainActivity)
                        .load(clickIcon)
                        .into(_binding.imageViewAction)
            }else {
                val startIcon = ListsRecyclerView.listIconAction[index]

                Glide
                        .with(this@MainActivity)
                        .load(startIcon)
                        .into(_binding.imageViewAction)
            }
        }
    }
}