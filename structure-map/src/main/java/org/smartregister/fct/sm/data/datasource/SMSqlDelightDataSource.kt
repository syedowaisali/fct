package org.smartregister.fct.sm.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.smartregister.fct.engine.util.compress
import org.smartregister.fct.engine.util.decompress
import org.smartregister.fct.logger.FCTLogger
import org.smartregister.fct.sm.domain.datasource.SMDataSource
import org.smartregister.fct.sm.domain.model.SMDetail
import sqldelight.SMDaoQueries

class SMSqlDelightDataSource(private val smDao: SMDaoQueries) : SMDataSource {

    override fun getAll(): Flow<List<SMDetail>> {
        return smDao
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map {
                it.map { sm ->
                    try {
                        SMDetail(
                            id = sm.id,
                            title = sm.title,
                            body = sm.body.decompress(),
                            source = sm.source.decompress(),
                        )
                    } catch (t: Throwable) {
                        FCTLogger.e(t)
                        SMDetail(
                            id = sm.id,
                            title = "Error",
                            body = t.toString(),
                            source = ""
                        )
                    }
                }
            }
    }

    override suspend fun insert(smDetail: SMDetail) {
        smDao.insert(
            id = smDetail.id,
            title = smDetail.title,
            body = smDetail.body.compress(),
            source = smDetail.source.compress()
        )
    }

    override suspend fun update(smDetail: SMDetail) {
        smDao.update(
            id = smDetail.id,
            title = smDetail.title,
            body = smDetail.body.compress(),
            source = smDetail.source.compress()
        )
    }

    override suspend fun delete(id: String) {
        smDao.delete(id)
    }
}