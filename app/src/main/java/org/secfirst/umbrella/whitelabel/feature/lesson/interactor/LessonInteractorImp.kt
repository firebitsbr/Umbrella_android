package org.secfirst.umbrella.whitelabel.feature.lesson.interactor

import org.secfirst.umbrella.whitelabel.data.database.lesson.LessonRepo
import org.secfirst.umbrella.whitelabel.data.database.lesson.Module
import org.secfirst.umbrella.whitelabel.feature.base.interactor.BaseInteractorImp
import javax.inject.Inject

class LessonInteractorImp @Inject constructor(private val lessonRepo: LessonRepo) : BaseInteractorImp(), LessonBaseInteractor {

    override suspend fun fetchMarkdownByModule(moduleId: Long) = lessonRepo.loadMarkdownByModule(moduleId)

    override suspend fun fetchMarkdownsBy(id: Long) = lessonRepo.loadMarkdownsBy(id)

    override suspend fun fetchMarkdownBySubject(id: Long) = lessonRepo.loadMarkdownBySubject(id)

    override suspend fun fetchDifficulty(id: Long) = lessonRepo.loadDifficultyBy(id)

    override suspend fun fetchLesson(id: Long) = lessonRepo.loadLessonBy(id)

    override suspend fun fetchDifficultyPreferredBy(subjectId: Long) = lessonRepo.loadDifficultyPreferredBy(subjectId)

    override suspend fun fetchSubject(id: Long) = lessonRepo.loadSubcategoryBy(id)

    override suspend fun fetchLessons(): List<Module> = lessonRepo.loadAllCategories()
}