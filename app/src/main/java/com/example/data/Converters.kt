package com.example.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromHackathonStatus(value: HackathonStatus?): String? = value?.name

    @TypeConverter
    fun toHackathonStatus(value: String?): HackathonStatus? =
        value?.let { runCatching { HackathonStatus.valueOf(it) }.getOrDefault(HackathonStatus.REGISTERED) }

    @TypeConverter
    fun fromFocusCategory(value: FocusCategory?): String? = value?.name

    @TypeConverter
    fun toFocusCategory(value: String?): FocusCategory? =
        value?.let { runCatching { FocusCategory.valueOf(it) }.getOrDefault(FocusCategory.CUSTOM) }

    @TypeConverter
    fun fromDsaPlatform(value: DsaPlatform?): String? = value?.name

    @TypeConverter
    fun toDsaPlatform(value: String?): DsaPlatform? =
        value?.let { runCatching { DsaPlatform.valueOf(it) }.getOrDefault(DsaPlatform.OTHER) }

    @TypeConverter
    fun fromDsaDifficulty(value: DsaDifficulty?): String? = value?.name

    @TypeConverter
    fun toDsaDifficulty(value: String?): DsaDifficulty? =
        value?.let { runCatching { DsaDifficulty.valueOf(it) }.getOrDefault(DsaDifficulty.MEDIUM) }

    @TypeConverter
    fun fromDsaStatus(value: DsaStatus?): String? = value?.name

    @TypeConverter
    fun toDsaStatus(value: String?): DsaStatus? =
        value?.let { runCatching { DsaStatus.valueOf(it) }.getOrDefault(DsaStatus.TO_DO) }

    @TypeConverter
    fun fromProjectStatus(value: ProjectStatus?): String? = value?.name

    @TypeConverter
    fun toProjectStatus(value: String?): ProjectStatus? =
        value?.let { runCatching { ProjectStatus.valueOf(it) }.getOrDefault(ProjectStatus.IDEA) }
}
