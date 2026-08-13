package com.lintang.sarprasq.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lintang.sarprasq.data.model.ActionPlan
import com.lintang.sarprasq.data.model.DamageReport
import com.lintang.sarprasq.data.model.HelpdeskReport
import com.lintang.sarprasq.data.model.KategoriMaster
import com.lintang.sarprasq.data.model.KondisiMaster
import com.lintang.sarprasq.data.model.SubKategoriMaster
import com.lintang.sarprasq.data.model.PeminjamanMakro
import com.lintang.sarprasq.data.model.ProjectTask
import com.lintang.sarprasq.data.model.Ruang
import com.lintang.sarprasq.data.model.SatuanMaster
import com.lintang.sarprasq.data.model.StatusPenanganan
import com.lintang.sarprasq.data.model.SuratArsip
import com.lintang.sarprasq.data.model.UrgensiMaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HelpdeskReport::class, ActionPlan::class, SuratArsip::class, PeminjamanMakro::class, Ruang::class, StatusPenanganan::class, UrgensiMaster::class, KategoriMaster::class, SubKategoriMaster::class, SatuanMaster::class, KondisiMaster::class, DamageReport::class, ProjectTask::class],
    version = 20,
    exportSchema = false
)
abstract class SarprasDatabase : RoomDatabase() {

    abstract fun sarprasDao(): SarprasDao

    companion object {
        @Volatile
        private var INSTANCE: SarprasDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SarprasDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SarprasDatabase::class.java,
                    "sarpras_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SarprasDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SarprasDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.sarprasDao())
                }
            }
        }

        suspend fun populateDatabase(dao: SarprasDao) {
            // Seed Master Data lists (Ruangan, Kategori, Status, Urgensi, Satuan, SubKategori, Kondisi)
            dao.insertRuangList(getDefaultRuangList())
            dao.insertKategoriList(getDefaultKategoriList())
            dao.insertStatusPenangananList(getDefaultStatusPenangananList())
            dao.insertUrgensiList(getDefaultUrgensiList())
            dao.insertSatuanList(getDefaultSatuanList())
            dao.insertSubKategoriList(getDefaultSubKategoriList())
            dao.insertKondisiList(getDefaultKondisiList())
        }
    }
}

fun getDefaultRuangList(): List<Ruang> {
    val list = mutableListOf<Ruang>()
    var idCounter = 1

    // R. Kelas X (A sampai J)
    listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J").forEach { letter ->
        list.add(
            Ruang(
                id = idCounter++,
                kodeRuang = "RK-X-$letter",
                namaRuang = "R. Kelas X $letter",
                kategori = "Ruang Kelas",
                penanggungJawab = "Wali Kelas X $letter"
            )
        )
    }

    // R. Kelas XI (A sampai J)
    listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J").forEach { letter ->
        list.add(
            Ruang(
                id = idCounter++,
                kodeRuang = "RK-XI-$letter",
                namaRuang = "R. Kelas XI $letter",
                kategori = "Ruang Kelas",
                penanggungJawab = "Wali Kelas XI $letter"
            )
        )
    }

    // R. Kelas XII (A sampai J)
    listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J").forEach { letter ->
        list.add(
            Ruang(
                id = idCounter++,
                kodeRuang = "RK-XII-$letter",
                namaRuang = "R. Kelas XII $letter",
                kategori = "Ruang Kelas",
                penanggungJawab = "Wali Kelas XII $letter"
            )
        )
    }

    // Ruangan Khusus / Fasilitas
    val specialRooms = listOf(
        Pair("R. TU", "Ruang Kerja"),
        Pair("R. BK", "Ruang Kerja"),
        Pair("PERPUSTAKAAN", "Fasilitas Umum"),
        Pair("SERVER", "Fasilitas Umum"),
        Pair("DAPUR", "Fasilitas Umum"),
        Pair("R. BIRO", "Ruang Kerja"),
        Pair("R. KESISWAAN", "Ruang Kerja"),
        Pair("MASJID", "Fasilitas Umum"),
        Pair("R. OSIS", "Fasilitas Umum"),
        Pair("R. PRAMUKA", "Fasilitas Umum"),
        Pair("INDOOR", "Fasilitas Umum"),
        Pair("R. GURU", "Ruang Kerja")
    )

    specialRooms.forEach { (nama, kat) ->
        val kode = "RM-" + nama.replace(".", "").replace(" ", "").uppercase()
        list.add(
            Ruang(
                id = idCounter++,
                kodeRuang = kode,
                namaRuang = nama,
                kategori = kat,
                penanggungJawab = "-"
            )
        )
    }

    return list
}

fun getDefaultKategoriList(): List<KategoriMaster> {
    val categories = listOf(
        Pair("Elektronik & Audio Visual", "Peralatan elektronik, proyektor, TV, sound system, & audio"),
        Pair("IT & Komputer", "Komputer, laptop, printer, periferal, networking & aksesoris IT"),
        Pair("Mebel / Furnitur", "Meja, kursi, lemari, rak, & papan tulis"),
        Pair("Perlengkapan Kebersihan & Rumah Tangga", "Sapu, tempat sampah, ember, kran, & alat kebersihan"),
        Pair("Alat Laboratorium / Praktikum", "Peralatan praktikum IPA, fisika, kimia, biologi, & sains"),
        Pair("Alat Olahraga", "Bola, net, matras, & fasilitas sarana olahraga"),
        Pair("Tools & Perkakas", "Obeng, tang, bor, palu, & peralatan perbaikan fisik"),
        Pair("ATK & Perlengkapan Kantor", "Alat tulis kantor, kertas, binder, & perlengkapan administrasi")
    )

    return categories.mapIndexed { index, (nama, desk) ->
        KategoriMaster(
            id = index + 1,
            namaKategori = nama,
            deskripsi = desk
        )
    }
}

fun getDefaultStatusPenangananList(): List<StatusPenanganan> {
    return listOf(
        StatusPenanganan(
            id = 1,
            namaStatus = "Catat",
            deskripsi = "Pencatatan rekapitulasi awal, laporan tetap berada di Helpdesk utama"
        ),
        StatusPenanganan(
            id = 2,
            namaStatus = "Pending",
            deskripsi = "Laporan atau pengajuan baru dikirim, menunggu verifikasi sarpras"
        ),
        StatusPenanganan(
            id = 3,
            namaStatus = "Proses",
            deskripsi = "Kerusakan sedang diperbaiki oleh teknisi atau pengadaan dalam proses"
        ),
        StatusPenanganan(
            id = 4,
            namaStatus = "Segera",
            deskripsi = "Pekerjaan mendesak / harus segera ditangani"
        ),
        StatusPenanganan(
            id = 5,
            namaStatus = "Selesai",
            deskripsi = "Perbaikan rampung, barang siap digunakan kembali, atau barang diterima"
        ),
        StatusPenanganan(
            id = 6,
            namaStatus = "Ditolak",
            deskripsi = "Pengajuan dibatalkan/ditolak karena anggaran kurang atau duplikat"
        )
    )
}

fun getDefaultUrgensiList(): List<UrgensiMaster> {
    return listOf(
        UrgensiMaster(
            id = 1,
            namaUrgensi = "Darurat / Kritis",
            deskripsi = "Menghentikan kegiatan belajar atau membahayakan keselamatan, misal: korsleting, plafon roboh"
        ),
        UrgensiMaster(
            id = 2,
            namaUrgensi = "Tinggi",
            deskripsi = "Fasilitas utama mendesak untuk kelas, misal: proyektor rusak saat ujian, AC ruang guru mati total"
        ),
        UrgensiMaster(
            id = 3,
            namaUrgensi = "Sedang",
            deskripsi = "Fasilitas pendukung, misal: kursi/meja goyang, wastafel mampet"
        ),
        UrgensiMaster(
            id = 4,
            namaUrgensi = "Rendah",
            deskripsi = "Bersifat opsional/jangka panjang, misal: pengecatan ulang, dekorasi"
        )
    )
}

fun getDefaultSatuanList(): List<SatuanMaster> {
    val items = listOf(
        "Batang", "Bungkus", "Dus", "Kg",
        "Lembar", "Liter", "Meter", "Pak",
        "Pcs", "Rim", "Roll", "Set",
        "Unit", "Botol", "Box", "Galon"
    )
    return items.mapIndexed { index, name ->
        SatuanMaster(
            id = index + 1,
            namaSatuan = name,
            deskripsi = "Satuan unit/ukur $name"
        )
    }
}

fun getDefaultSubKategoriList(): List<SubKategoriMaster> {
    return listOf(
        SubKategoriMaster(
            id = 1,
            namaSubKategori = "Pemeliharaan Berkala",
            deskripsi = "Perawatan dan pemeliharaan rutin sarana prasarana sekolah"
        ),
        SubKategoriMaster(
            id = 2,
            namaSubKategori = "Perbaikan Minor",
            deskripsi = "Perbaikan skala kecil yang dapat ditangani dengan cepat"
        ),
        SubKategoriMaster(
            id = 3,
            namaSubKategori = "Perbaikan Mayor",
            deskripsi = "Perbaikan kerusakan tingkat sedang hingga berat yang membutuhkan perencanaan"
        ),
        SubKategoriMaster(
            id = 4,
            namaSubKategori = "Pengadaan Barang Baru",
            deskripsi = "Pengadaan baru fasilitas, peralatan, atau perlengkapan pendukung"
        )
    )
}

fun getDefaultKondisiList(): List<KondisiMaster> {
    return listOf(
        KondisiMaster(
            id = 1,
            namaKondisi = "Baik",
            deskripsi = "Barang/sarpras dalam kondisi prima, berfungsi normal 100% tanpa kendala"
        ),
        KondisiMaster(
            id = 2,
            namaKondisi = "Rusak Sedang",
            deskripsi = "Barang/sarpras mengalami kendala fungsi sebagian namun masih dapat digunakan dengan catatan"
        ),
        KondisiMaster(
            id = 3,
            namaKondisi = "Rusak",
            deskripsi = "Barang/sarpras mengalami kerusakan total atau tidak dapat digunakan sama sekali"
        ),
        KondisiMaster(
            id = 4,
            namaKondisi = "Perawatan",
            deskripsi = "Barang/sarpras sedang dalam tahap pemeliharaan, servis, atau perbaikan teknis"
        )
    )
}
