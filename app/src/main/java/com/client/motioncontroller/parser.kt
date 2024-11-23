package com.client.motioncontroller

fun parser(value : ByteArray) {

    if (value[0].toInt().toChar() == 'R') {

        //0 репорт
        if (value[0].toUInt() == 0u) {



            if (value[2].toUInt() and 0x1u == 1u) {
                println("!!! Мотор включен")
                statusDriver.value = statusDriver.value.copy(enable = true)
            } else {
                println("!!! Мотор выключен")
                statusDriver.value = statusDriver.value.copy(enable = false)
            }




        }






    }

}