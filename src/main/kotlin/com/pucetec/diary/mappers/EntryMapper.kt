package com.pucetec.diary.mappers

import com.pucetec.diary.dto.EntryResponse
import com.pucetec.diary.entities.Entry
import org.springframework.stereotype.Component

@Component
class EntryMapper {

    fun toResponse(entry: Entry): EntryResponse =
        EntryResponse(
            id = entry.id,
            title = entry.title,
            body = entry.body,
            author = entry.author,
            createdAt = entry.createdAt
        )

    fun toResponseList(entries: List<Entry>): List<EntryResponse> = entries.map { toResponse(it) }
}
