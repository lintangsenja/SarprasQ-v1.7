package com.lintang.sarprasq.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.DamageReport
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.KategoriMaster
import com.lintang.sarprasq.data.model.SubKategoriMaster
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SatuanMaster
import com.lintang.sarprasq.data.model.StatusPenanganan
import com.lintang.sarprasq.data.model.SuratArsip
import com.lintang.sarprasq.data.model.UrgensiMaster
import kotlinx.coroutines.flow.Flow

@Dao
interface SarprasDao {

    // --- DAMAGE REPORTS (MULTI-ITEM PER ROOM) ---
    @Query("SELECT * FROM damage_reports ORDER BY timestamp DESC")
    fun getAllDamageReports(): Flow<List<DamageReport>>

    @Query("SELECT * FROM damage_reports WHERE roomId = :roomId ORDER BY timestamp DESC")
    fun getDamageReportsByRoom(roomId: Int): Flow<List<DamageReport>>

    @Query("SELECT * FROM damage_reports ORDER BY timestamp DESC")
    suspend fun getAllDamageReportsList(): List<DamageReport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDamageReport(report: DamageReport): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDamageReportList(list: List<DamageReport>)

    @Update
    suspend fun updateDamageReport(report: DamageReport)

    @Delete
    suspend fun deleteDamageReport(report: DamageReport)

    @Query("DELETE FROM damage_reports")
    suspend fun deleteAllDamageReports()

    // --- HELPDESK REPORTS ---
    @Query("SELECT * FROM helpdesk_reports ORDER BY timestamp DESC")
    fun getAllHelpdeskReports(): Flow<List<HelpdeskReport>>

    @Query("SELECT * FROM helpdesk_reports ORDER BY timestamp DESC")
    suspend fun getAllHelpdeskReportsList(): List<HelpdeskReport>

    @Query("SELECT * FROM helpdesk_reports WHERE status = 'Pending' ORDER BY timestamp DESC")
    fun getPendingReports(): Flow<List<HelpdeskReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelpdeskReport(report: HelpdeskReport): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHelpdeskReports(reports: List<HelpdeskReport>)

    @Update
    suspend fun updateHelpdeskReport(report: HelpdeskReport)

    @Delete
    suspend fun deleteHelpdeskReport(report: HelpdeskReport)

    @Query("DELETE FROM helpdesk_reports")
    suspend fun deleteAllHelpdeskReports()

    // --- ACTION PLANS ---
    @Query("SELECT * FROM action_plans ORDER BY timestamp DESC")
    fun getAllActionPlans(): Flow<List<ActionPlan>>

    @Query("SELECT * FROM action_plans ORDER BY timestamp DESC")
    suspend fun getAllActionPlansList(): List<ActionPlan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionPlan(plan: ActionPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionPlans(plans: List<ActionPlan>)

    @Update
    suspend fun updateActionPlan(plan: ActionPlan)

    @Delete
    suspend fun deleteActionPlan(plan: ActionPlan)

    @Query("DELETE FROM action_plans")
    suspend fun deleteAllActionPlans()

    // --- SURAT ARSIP ---
    @Query("SELECT * FROM surat_arsip ORDER BY timestamp DESC")
    fun getAllSuratArsip(): Flow<List<SuratArsip>>

    @Query("SELECT * FROM surat_arsip ORDER BY timestamp DESC")
    suspend fun getAllSuratArsipList(): List<SuratArsip>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuratArsip(surat: SuratArsip): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuratArsipList(suratList: List<SuratArsip>)

    @Delete
    suspend fun deleteSuratArsip(surat: SuratArsip)

    @Query("DELETE FROM surat_arsip")
    suspend fun deleteAllSuratArsip()

    // --- PEMINJAMAN MAKRO ---
    @Query("SELECT * FROM peminjaman_makro ORDER BY timestamp DESC")
    fun getAllPeminjamanMakro(): Flow<List<PeminjamanMakro>>

    @Query("SELECT * FROM peminjaman_makro ORDER BY timestamp DESC")
    suspend fun getAllPeminjamanMakroList(): List<PeminjamanMakro>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeminjamanMakro(peminjaman: PeminjamanMakro): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeminjamanMakroList(peminjamanList: List<PeminjamanMakro>)

    @Delete
    suspend fun deletePeminjamanMakro(peminjaman: PeminjamanMakro)

    @Query("DELETE FROM peminjaman_makro")
    suspend fun deleteAllPeminjamanMakro()

    // --- MASTER DATA: RUANG ---
    @Query("SELECT * FROM master_ruang ORDER BY namaRuang ASC")
    fun getAllRuang(): Flow<List<Ruang>>

    @Query("SELECT * FROM master_ruang ORDER BY namaRuang ASC")
    suspend fun getAllRuangList(): List<Ruang>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuang(ruang: Ruang): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuangList(list: List<Ruang>)

    @Update
    suspend fun updateRuang(ruang: Ruang)

    @Delete
    suspend fun deleteRuang(ruang: Ruang)

    @Query("DELETE FROM master_ruang")
    suspend fun deleteAllRuang()

    // --- MASTER DATA: STATUS PENANGANAN ---
    @Query("SELECT * FROM master_status_penanganan ORDER BY id ASC")
    fun getAllStatusPenanganan(): Flow<List<StatusPenanganan>>

    @Query("SELECT * FROM master_status_penanganan ORDER BY id ASC")
    suspend fun getAllStatusPenangananList(): List<StatusPenanganan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusPenanganan(status: StatusPenanganan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusPenangananList(list: List<StatusPenanganan>)

    @Update
    suspend fun updateStatusPenanganan(status: StatusPenanganan)

    @Delete
    suspend fun deleteStatusPenanganan(status: StatusPenanganan)

    @Query("DELETE FROM master_status_penanganan")
    suspend fun deleteAllStatusPenanganan()

    // --- MASTER DATA: URGENSI ---
    @Query("SELECT * FROM master_urgensi ORDER BY id ASC")
    fun getAllUrgensi(): Flow<List<UrgensiMaster>>

    @Query("SELECT * FROM master_urgensi ORDER BY id ASC")
    suspend fun getAllUrgensiList(): List<UrgensiMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUrgensi(urgensi: UrgensiMaster): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUrgensiList(list: List<UrgensiMaster>)

    @Update
    suspend fun updateUrgensi(urgensi: UrgensiMaster)

    @Delete
    suspend fun deleteUrgensi(urgensi: UrgensiMaster)

    @Query("DELETE FROM master_urgensi")
    suspend fun deleteAllUrgensi()

    // --- MASTER DATA: KATEGORI ---
    @Query("SELECT * FROM master_kategori ORDER BY id ASC")
    fun getAllKategori(): Flow<List<KategoriMaster>>

    @Query("SELECT * FROM master_kategori ORDER BY id ASC")
    suspend fun getAllKategoriList(): List<KategoriMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKategori(kategori: KategoriMaster): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKategoriList(list: List<KategoriMaster>)

    @Update
    suspend fun updateKategori(kategori: KategoriMaster)

    @Delete
    suspend fun deleteKategori(kategori: KategoriMaster)

    @Query("DELETE FROM master_kategori")
    suspend fun deleteAllKategori()

    // --- MASTER DATA: SATUAN ---
    @Query("SELECT * FROM master_satuan ORDER BY id ASC")
    fun getAllSatuan(): Flow<List<SatuanMaster>>

    @Query("SELECT * FROM master_satuan ORDER BY id ASC")
    suspend fun getAllSatuanList(): List<SatuanMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatuan(satuan: SatuanMaster): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSatuanList(list: List<SatuanMaster>)

    @Update
    suspend fun updateSatuan(satuan: SatuanMaster)

    @Delete
    suspend fun deleteSatuan(satuan: SatuanMaster)

    @Query("DELETE FROM master_satuan")
    suspend fun deleteAllSatuan()

    // --- MASTER DATA: SUB KATEGORI / SIFAT PEKERJAAN ---
    @Query("SELECT * FROM master_sub_kategori ORDER BY id ASC")
    fun getAllSubKategori(): Flow<List<SubKategoriMaster>>

    @Query("SELECT * FROM master_sub_kategori ORDER BY id ASC")
    suspend fun getAllSubKategoriList(): List<SubKategoriMaster>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubKategori(subKategori: SubKategoriMaster): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubKategoriList(list: List<SubKategoriMaster>)

    @Update
    suspend fun updateSubKategori(subKategori: SubKategoriMaster)

    @Delete
    suspend fun deleteSubKategori(subKategori: SubKategoriMaster)

    @Query("DELETE FROM master_sub_kategori")
    suspend fun deleteAllSubKategori()

    // --- PROJECT TASKS / TO-DO ---
    @Query("SELECT * FROM project_tasks ORDER BY timestamp DESC")
    fun getAllProjectTasks(): Flow<List<ProjectTask>>

    @Query("SELECT * FROM project_tasks ORDER BY timestamp DESC")
    suspend fun getAllProjectTasksList(): List<ProjectTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectTask(task: ProjectTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectTasks(tasks: List<ProjectTask>)

    @Update
    suspend fun updateProjectTask(task: ProjectTask)

    @Delete
    suspend fun deleteProjectTask(task: ProjectTask)

    @Query("DELETE FROM project_tasks")
    suspend fun deleteAllProjectTasks()
}
