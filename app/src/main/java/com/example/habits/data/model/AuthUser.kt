package com.example.habits.data.model

/** Model: usuário autenticado, desacoplado das classes do Firebase. */
data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
) {
    /** Primeiro nome para saudação ("Olá, Rani!"); usa o início do e-mail se não houver nome. */
    val firstName: String
        get() = displayName?.trim()?.split(" ")?.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: ""

    /** Iniciais para o avatar ("RF"). */
    val initials: String
        get() {
            val parts = displayName?.trim()?.split(Regex("\\s+"))?.filter { it.isNotEmpty() }.orEmpty()
            val letters = when {
                parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}"
                parts.size == 1 -> parts.first().take(1)
                else -> email?.take(1).orEmpty()
            }
            return letters.uppercase()
        }
}
