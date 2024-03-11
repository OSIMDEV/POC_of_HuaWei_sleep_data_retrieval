package com.osim.health.algorithms

import android.util.Log
import kotlin.math.max
import kotlin.math.min

fun score(d: Double, s: Double, t: Double, age: Double): Double {
    var ret = -1.0
    val checkedArgs = _checkArgs(d, s, t, age)
    if (checkedArgs.isNotEmpty()) {
        val (_d, _s, _t, _age) = checkedArgs
        ret = _score(_d, _s, _t, _age)
    }
    return ret
}

private fun _checkArgs(d: Double, s: Double, t: Double, age: Double): List<Double> {
    val _age = limit(age, 0.0, 100.0)
    val _d = limit(d, 0.0, _osdD(age))
    var _s = limit(s, 0.0, d)
    var _t = limit(t, 0.0, d)
    if (_s + _t > _d) {
        return emptyList()
    }
    if (_d != 0.0) {
        _s /= _d
        _t /= _d
    }
    return listOf(_d, _s, _t, _age)
}

private fun _score(d: Double, s: Double, t: Double, age: Double): Double {
    val (w0, w1, w2) = listOf(0.3, 0.4, 0.3)
    return 100.0 *
            min(
                w0 * _osd(d, age) + w1 * _ratioDeep(s, age) + w2 * _ratioRem(t, age),
                1.0,
            )
}

private fun limit(t: Double, low: Double, high: Double): Double {
    return max(low, min(t, high))
}

private fun scale(t: Double, low: Double, high: Double): Double {
    return (1.0 - t) * low + t * high
}

private fun _osd(d: Double, age: Double): Double {
    val dAge = _osdD(age)
    var ret = 0.0
    if (d < 3.0) {
        ret = 0.0
    } else if (d < (dAge + 3.0) / 2.0) {
        ret = (2.0 * (d - 3.0)) / (3.0 * (dAge - 3.0))
    } else if (d < dAge) {
        ret = (4.0 * d - dAge - 9.0) / (3.0 * (dAge - 3.0))
    } else {
        ret = 1.0
    }
    return ret
}

private fun _osdD(age: Double): Double {
    var ret = 0.0
    if (age < 18.0) {
        ret = 10.0
    } else if (age < 65.0) {
        ret = 9.0
    } else {
        ret = 8.0
    }
    return ret
}

private fun _ratioDeep(s: Double, age: Double): Double {
    val rAge = _deepR(age)
    var ret = 0.0
    if (s < 0.0) {
        ret = 0.0
    } else if (s < rAge / 2.0) {
        ret = (2.0 * s) / (3.0 * rAge)
    } else if (s < rAge) {
        ret = (4.0 * s - rAge) / (3.0 * rAge)
    } else {
        ret = 1.0
    }
    return ret
}

private fun _deepR(age: Double): Double {
    var ret = 0.0
    if (age < 18.0) {
        ret = 0.25
    } else if (age < 65.0) {
        ret = 0.2
    } else {
        ret = 0.15
    }
    return ret
}

private fun _ratioRem(t: Double, age: Double): Double {
    var rAge = _remR(age)
    var ret = 0.0
    if (t < 0.0) {
        ret = 0.0
    } else if (t < rAge / 2.0) {
        ret = (2.0 * t) / (3.0 * rAge)
    } else if (t < rAge) {
        ret = (4.0 * t - rAge) / (3.0 * rAge)
    } else {
        ret = 1.0
    }
    return ret
}

private fun _remR(age: Double): Double {
    var ret = 0.0
    if (age < 18.0) {
        ret = 0.25
    } else if (age < 65.0) {
        ret = 0.25
    } else {
        ret = 0.25
    }
    return ret
}