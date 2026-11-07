package com.example.habits

import com.example.habits.data.model.AuthUser
import com.example.habits.ui.auth.AuthValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun login_requiresValidEmailAndPassword() {
        assertNotNull(AuthValidator.validateLogin("invalido", "123456"))
        assertNotNull(AuthValidator.validateLogin("rani@email.com", ""))
        assertNull(AuthValidator.validateLogin("rani@email.com", "123456"))
    }

    @Test
    fun register_checksNamePasswordConfirmationAndTerms() {
        assertNotNull(AuthValidator.validateRegister("", "rani@email.com", "123456", "123456", true))
        assertNotNull(AuthValidator.validateRegister("Rani", "rani@email.com", "123", "123", true))
        assertEquals("As senhas não coincidem.", AuthValidator.validateRegister("Rani", "rani@email.com", "123456", "654321", true))
        assertNotNull(AuthValidator.validateRegister("Rani", "rani@email.com", "123456", "123456", false))
        assertNull(AuthValidator.validateRegister("Rani", "rani@email.com", "123456", "123456", true))
    }

    @Test
    fun authUser_buildsGreetingAndInitials() {
        val user = AuthUser("1", "rani@email.com", "Ranielly Ferreira")
        assertEquals("Ranielly", user.firstName)
        assertEquals("RF", user.initials)
        assertEquals("Rani", AuthUser("2", "rani@email.com", null).firstName)
    }
}
