package com.example.habits.ui.components

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")
private val MonthFormatter = DateTimeFormatter.ofPattern("MMMM", PtBr)

val DayOfWeek.fullLabel: String
    get() = when (this) {
        DayOfWeek.MONDAY -> "Segunda"
        DayOfWeek.TUESDAY -> "Terça"
        DayOfWeek.WEDNESDAY -> "Quarta"
        DayOfWeek.THURSDAY -> "Quinta"
        DayOfWeek.FRIDAY -> "Sexta"
        DayOfWeek.SATURDAY -> "Sábado"
        DayOfWeek.SUNDAY -> "Domingo"
    }

/** "Seg", "Ter", ... */
val DayOfWeek.shortLabel: String get() = fullLabel.take(3)

/** "D", "S", "T", ... (seletor de dias da semana). */
val DayOfWeek.initial: String get() = fullLabel.take(1)

fun LocalDate.monthName(): String = format(MonthFormatter)

/** "Sexta, 6 de novembro" */
fun LocalDate.headerLabel(): String = "${dayOfWeek.fullLabel}, $dayOfMonth de ${monthName()}"

/** "2 – 8 de novembro" ou "30 de outubro – 5 de novembro" */
fun rangeLabel(start: LocalDate, end: LocalDate): String =
    if (start.month == end.month) "${start.dayOfMonth} – ${end.dayOfMonth} de ${end.monthName()}"
    else "${start.dayOfMonth} de ${start.monthName()} – ${end.dayOfMonth} de ${end.monthName()}"
