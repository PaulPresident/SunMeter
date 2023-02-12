package com.paul.sunmeter

import kotlin.math.pow

/** This file contains formulas for calculating daily percentage of sun exposure based on UVI.
 *  - DATA: https://www.gbhealthwatch.com/Did-you-know-Get-VitD-Sun-Exposure.php
 *  - Desmos: https://www.desmos.com/calculator/ilntjffnru
 */

val type1 = { uvi : Int -> 60.3837 * 0.965606.pow(uvi) - 39.8448 }
val type2 = { uvi : Int -> 68.6724 * 0.956083.pow(uvi) - 39.0141 }
val type3 = { uvi : Int -> 64.4192 * 0.934167.pow(uvi) - 23.6698 }
val type4 = { uvi : Int -> 79.8481 * 0.927704.pow(uvi) - 23.9451 }
val type5 = { uvi : Int -> 94.8762 * 0.873641.pow(uvi) - 4.69209 }