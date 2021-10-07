package com.example.logiceditor.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.text.Layout
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.logiceditor.R
import java.io.File

fun AppCompatActivity.createSaveAlertDialog(mainActivity: MainActivity, isExit: Boolean = false) =
    with(AlertDialog.Builder(mainActivity)) {
        setTitle("Сохранение схемы")
        val view = mainActivity.layoutInflater.inflate(R.layout.save_dialog,null)
        val editTextName = view.findViewById<EditText>(R.id.editTextTextPersonName)
        if (MainActivity.nameDate.first != "" && MainActivity.nameDate.second != "") {
            editTextName.setText(MainActivity.nameDate.first, TextView.BufferType.EDITABLE)
        }
        setView(view)
        setPositiveButton("Сохранить") { currentDialog, _ ->
            val name = editTextName.text.toString().trim()
            if (name.isNotEmpty()) {
                val checkName = mainActivity.isExistNameCircuit(name)
                if (checkName != null) {
                    createSameNameAlertDialog(mainActivity, checkName, currentDialog as AlertDialog, isExit).show()
                }else {
                    WelcomeActivity.isChangeListCircuit = true
                    mainActivity.saveCircuit(name)
                    if (!isExit) {
                        Toast.makeText(mainActivity, "Схема \"$name\" успешно сохранена", Toast.LENGTH_SHORT).show()
                        mainActivity.updateToolWireCount()
                        currentDialog.cancel()
                    }else {
                        mainActivity.finishAndRemoveTask()
                    }
                }
            }else {
                Toast.makeText(mainActivity, "Введите корректное название!!!", Toast.LENGTH_SHORT).show()
            }
        }
        setNegativeButton("Отмена") { currentDialog, _ ->
            currentDialog.cancel()
        }
        setCancelable(true)
        create()
    }

private fun AppCompatActivity.createSameNameAlertDialog(mainActivity: MainActivity, name: String, saveDialog: AlertDialog, isExit: Boolean = false) =
        with(AlertDialog.Builder(mainActivity)) {
            setTitle("Предупреждение!!!")
            setMessage("Файл с таким именем уже существует. Хотите перезаписать его?")
            setPositiveButton("Да") { currentDialog, _ ->
                WelcomeActivity.isChangeListCircuit = true
                mainActivity.saveCircuit(name, isRewrite = true)
                mainActivity.updateToolWireCount()

                val circuitName = name.substringBefore("_DATE_")

                if (!isExit) {
                    Toast.makeText(mainActivity, "Схема \"$circuitName\" успешно сохранена", Toast.LENGTH_SHORT).show()
                    mainActivity.updateToolWireCount()
                    saveDialog.cancel()
                    currentDialog.cancel()
                }else {
                    mainActivity.finishAndRemoveTask()
                }
            }
            setNegativeButton("Отмена") { currentDialog, _ ->
                currentDialog.cancel()
            }
            setCancelable(true)
            create()
        }

fun AppCompatActivity.createNotSaveCircuitAlertDialog(mainActivity: MainActivity) =
        with(AlertDialog.Builder(mainActivity)) {
            setTitle("Предупреждение!!!")
            setMessage("Схема не сохранена! Хотите выйти без сохранения?")
            setPositiveButton("Да") { currentDialog, _ ->
                currentDialog.cancel()
                mainActivity.finishAndRemoveTask()
            }
            setNegativeButton("Сохранить") { currentDialog, _ ->
                createSaveAlertDialog(mainActivity, isExit = true).show()
                currentDialog.cancel()
            }
            setCancelable(true)
            create()
        }

@SuppressLint("WrongConstant")
fun AppCompatActivity.createReferenceAlertDialog() =
        with(AlertDialog.Builder(this)) {
            setTitle("Справка")
            val view = this@createReferenceAlertDialog.layoutInflater.inflate(R.layout.reference_main_activity,null)
            val textViewReference = view.findViewById<TextView>(R.id.textViewReference)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                textViewReference.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
            }
            setView(view)
            setPositiveButton("Ок") {dialog,_ -> dialog.cancel()}
            create()
        }

@SuppressLint("WrongConstant")
fun AppCompatActivity.createReferenceWelcomeActivityAlertDialog() =
        with(AlertDialog.Builder(this)) {
            setTitle("Справка")
            val view = this@createReferenceWelcomeActivityAlertDialog.layoutInflater.inflate(R.layout.reference_welcome_activity,null)
            val textViewReference = view.findViewById<TextView>(R.id.textViewReferenceWelcomeActivity)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                textViewReference.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
            }
            setView(view)
            setPositiveButton("Ок") {dialog,_ -> dialog.cancel()}
            create()
        }

fun Context.createPopupMenu(menuResource: Int, anchor: View): PopupMenu {
    val popupMenu = PopupMenu(this,anchor)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        popupMenu.gravity = Gravity.RIGHT
    }
    popupMenu.menuInflater.inflate(menuResource,popupMenu.menu)
    return popupMenu
}

fun AppCompatActivity.createRenameAlertDialog(welcomeActivity: WelcomeActivity, dir: String, fileName: String) =
        with(AlertDialog.Builder(welcomeActivity)) {
            setTitle("Переименование схемы")
            val view = welcomeActivity.layoutInflater.inflate(R.layout.save_dialog,null)
            val editTextName = view.findViewById<EditText>(R.id.editTextTextPersonName)
            editTextName.setText(fileName.substringBefore("_DATE_"), TextView.BufferType.EDITABLE)
            setView(view)
            setPositiveButton("Переименовать") { currentDialog, _ ->
                val name = editTextName.text.toString().trim()
                if (name.isNotEmpty()) {
                    val oldFile = File(dir, fileName)
                    val date = CurrentDate.getCurrentDate()
                    val newFileName = "${name}_DATE_${date}"
                    val newFile = File(dir, newFileName)
                    oldFile.renameTo(newFile)
                    welcomeActivity.updateRecyclerView()
                    Toast.makeText(welcomeActivity, "Схема \"${fileName.substringBefore("_DATE_")}\" переименована в \"$name\"", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(welcomeActivity, "Введите корректное название!!!", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Отмена") { currentDialog, _ ->
                currentDialog.cancel()
            }
            setCancelable(true)
            create()
        }

fun AppCompatActivity.createConfirmDeleteCircuitAlertDialog(welcomeActivity: WelcomeActivity, fileName: String) =
        with(AlertDialog.Builder(welcomeActivity)) {
            setTitle("Хотите удалить схему?")
            setPositiveButton("Удалить") { currentDialog, _ ->
                currentDialog.cancel()
                welcomeActivity.deleteCircuit(fileName)
                welcomeActivity.updateRecyclerView()
                Toast.makeText(welcomeActivity, "Схема \"${fileName.substringBefore("_DATE_")}\" успешно удалена", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("Отмена") { currentDialog, _ ->
                currentDialog.cancel()
            }
            setCancelable(true)
            create()
        }