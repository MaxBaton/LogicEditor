package com.example.logiceditor.activity

import com.example.logiceditor.R

object ListsRecyclerView {
    //elements
    val listNameElements = listOf("И", "ИЛИ", "НЕ",  "Исключающее ИЛИ", "Кнопка", "Лампочка")
    val listIconElements = listOf(R.drawable.and_element, R.drawable.or_element, R.drawable.not_elemet, R.drawable.xor_element,
                                    R.drawable.button_off_recycler_view, R.drawable.light_recycler_view)
    //actions
    val listIconAction = listOf(R.drawable.wire, R.drawable.start, R.drawable.delete, R.drawable.inversion,
                                    R.drawable.save, R.drawable.reference, R.drawable.exit)

    val listIconActionClick = listOf(R.drawable.wire_click, R.drawable.stop, R.drawable.delete_click, R.drawable.inversion_click)
}