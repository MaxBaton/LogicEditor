package com.example.logiceditor.tools.elements

interface LogicCalculation {
    fun calculate(input1: Boolean, input2: Boolean = false): Boolean
}