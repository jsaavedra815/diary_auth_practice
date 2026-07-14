package com.pucetec.diary.controllers

import com.pucetec.diary.config.SecurityConfig
import com.pucetec.diary.entities.Entry
import com.pucetec.diary.mappers.EntryMapper
import com.pucetec.diary.services.EntryService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(EntryController::class)
@Import(SecurityConfig::class)   // el slice de @WebMvcTest no carga las @Configuration por su cuenta
class EntryControllerSecurityTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var entryService: EntryService

    @MockitoBean
    lateinit var entryMapper: EntryMapper

    // Fabrica un access_token de Cognito falso: solo nos importa el claim `username`.
    private fun tokenOf(username: String) = jwt().jwt { it.claim("username", username) }

    @Test
    fun `sin token, listar mis entradas devuelve 401`() {
        mockMvc.perform(get("/entries"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `sin token, escribir una entrada devuelve 401`() {
        mockMvc.perform(
            post("/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"x","body":"y"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `el service recibe el username del TOKEN, no de un parametro`() {
        whenever(entryService.findMine("ana")).thenReturn(emptyList())
        whenever(entryMapper.toResponseList(any())).thenReturn(emptyList())

        mockMvc.perform(get("/entries").with(tokenOf("ana")))
            .andExpect(status().isOk)

        // La prueba de que NO se puede suplantar a nadie: el username salió del JWT.
        verify(entryService).findMine("ana")
    }

    @Test
    fun `el autor de una entrada nueva sale del TOKEN, aunque el body diga otra cosa`() {
        val entry = Entry(title = "Martes horrible", body = "...", author = "ana")
        whenever(entryService.create(any(), any(), any())).thenReturn(entry)
        whenever(entryMapper.toResponse(any())).thenReturn(
            com.pucetec.diary.dto.EntryResponse(1, "Martes horrible", "...", "ana", entry.createdAt)
        )

        mockMvc.perform(
            post("/entries").with(tokenOf("ana"))
                .contentType(MediaType.APPLICATION_JSON)
                // El cliente intenta colar un autor en el body. Jackson lo ignora:
                // lo que no está en el DTO, no existe.
                .content("""{"title":"Martes horrible","body":"...","author":"beto"}""")
        ).andExpect(status().isCreated)

        verify(entryService).create("Martes horrible", "...", "ana")
    }
}
