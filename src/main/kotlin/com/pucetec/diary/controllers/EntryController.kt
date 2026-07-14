package com.pucetec.diary.controllers

import com.pucetec.diary.dto.EntryRequest
import com.pucetec.diary.dto.EntryResponse
import com.pucetec.diary.mappers.EntryMapper
import com.pucetec.diary.services.EntryService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * ⚠️ VERSIÓN 1 — CHECKPOINT 1. Tiene un agujero enorme, y es a propósito.
 *
 * El autor viene por `?author=`, o sea que el CLIENTE está afirmando quién es.
 * Una afirmación del cliente no vale nada: basta con escribir el nombre de otro
 * en la URL para leerle el diario.
 *
 *     GET /entries?author=beto   →  200  😱
 *
 * En el Checkpoint 3 el `?author=` desaparece de todos los endpoints y el "yo"
 * pasa a decidirlo el JWT.
 */
@RestController
@RequestMapping("/entries")
class EntryController(
    private val entryService: EntryService,
    private val entryMapper: EntryMapper
) {

    @GetMapping
    fun mine(@RequestParam author: String): List<EntryResponse> =
        entryMapper.toResponseList(entryService.findMine(author))

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody request: EntryRequest,
        @RequestParam author: String
    ): EntryResponse =
        entryMapper.toResponse(entryService.create(request.title, request.body, author))

    @GetMapping("/{id}")
    fun one(
        @PathVariable id: Long,
        @RequestParam author: String
    ): EntryResponse =
        entryMapper.toResponse(entryService.findOne(id, author))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: EntryRequest,
        @RequestParam author: String
    ): EntryResponse =
        entryMapper.toResponse(entryService.update(id, request.title, request.body, author))

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: Long,
        @RequestParam author: String
    ) = entryService.delete(id, author)
}
