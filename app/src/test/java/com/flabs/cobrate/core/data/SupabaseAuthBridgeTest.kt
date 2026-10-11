package com.flabs.cobrate.core.data

import com.flabs.cobrate.core.logic.AuthErrorUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

class SupabaseAuthBridgeTest {

    @Test
    fun testRefreshTokenNotFoundIsNotNetworkError() {
        val rawErrorMessage = "io.github.jan.supabase.auth.exception.AuthRestException: refresh_token_not_found\n" +
                "Invalid Refresh Token: Refresh Token Not Found: refresh_token_not_found\n" +
                "URL: https://ianiulmdyzsqtrqpujkx.supabase.co/auth/v1/token?grant_type=refresh_token"
        val exception = Exception(rawErrorMessage)

        val esRed = AuthErrorUtils.esErrorDeRed(exception)

        assertFalse("refresh_token_not_found NO debe ser clasificado como error de red", esRed)
    }

    @Test
    fun testAuthRestExceptionStringIsNotNetworkError() {
        val exception = Exception("io.github.jan.supabase.auth.exception.AuthRestException: invalid_grant")

        val esRed = AuthErrorUtils.esErrorDeRed(exception)

        assertFalse("AuthRestException / invalid_grant NO debe ser clasificado como error de red", esRed)
    }

    @Test
    fun testUnknownHostExceptionIsNetworkError() {
        val exception = UnknownHostException("Unable to resolve host ianiulmdyzsqtrqpujkx.supabase.co")

        val esRed = AuthErrorUtils.esErrorDeRed(exception)

        assertTrue("UnknownHostException SÍ debe ser clasificado como error de red", esRed)
    }

    @Test
    fun testConnectExceptionIsNetworkError() {
        val exception = ConnectException("Failed to connect to /100.20.30.40:443")

        val esRed = AuthErrorUtils.esErrorDeRed(exception)

        assertTrue("ConnectException SÍ debe ser clasificado como error de red", esRed)
    }

    @Test
    fun testGenericIOExceptionIsNetworkError() {
        val exception = IOException("Network socket reset by peer")

        val esRed = AuthErrorUtils.esErrorDeRed(exception)

        assertTrue("IOException SÍ debe ser clasificado como error de red", esRed)
    }
}
