package com.flabs.cobrate.features.reportes

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Una celda de la hoja. */
sealed class Celda {
    data class Texto(val valor: String) : Celda()
    data class Encabezado(val valor: String) : Celda()
    data class Numero(val valor: Double) : Celda()
}

/**
 * Escribe un .xlsx de una sola hoja ("Cobros") sin librerías: un zip con las partes mínimas de
 * Office Open XML. shortcut: sin fórmulas ni varias hojas; subir a una librería si hacen falta.
 */
object XlsxWriter {

    fun escribir(filas: List<List<Celda>>, salida: OutputStream) {
        ZipOutputStream(salida).use { zip ->
            parte(zip, "[Content_Types].xml", CONTENT_TYPES)
            parte(zip, "_rels/.rels", RELS)
            parte(zip, "xl/workbook.xml", WORKBOOK)
            parte(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS)
            parte(zip, "xl/styles.xml", STYLES)
            parte(zip, "xl/worksheets/sheet1.xml", hoja(filas))
        }
    }

    private fun parte(zip: ZipOutputStream, nombre: String, contenido: String) {
        zip.putNextEntry(ZipEntry(nombre))
        zip.write(contenido.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun hoja(filas: List<List<Celda>>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("""<cols><col min="1" max="2" width="12" customWidth="1"/><col min="3" max="3" width="16" customWidth="1"/>""")
        sb.append("""<col min="4" max="4" width="14" customWidth="1"/><col min="5" max="5" width="22" customWidth="1"/><col min="6" max="6" width="14" customWidth="1"/></cols>""")
        sb.append("<sheetData>")
        filas.forEachIndexed { i, fila ->
            sb.append("""<row r="${i + 1}">""")
            fila.forEachIndexed { j, celda ->
                val ref = "${columna(j)}${i + 1}"
                when (celda) {
                    is Celda.Texto -> sb.append("""<c r="$ref" t="inlineStr"><is><t>${escapar(celda.valor)}</t></is></c>""")
                    is Celda.Encabezado -> sb.append("""<c r="$ref" s="1" t="inlineStr"><is><t>${escapar(celda.valor)}</t></is></c>""")
                    is Celda.Numero -> sb.append("""<c r="$ref" s="2"><v>${celda.valor}</v></c>""")
                }
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    /** 0 → A, 1 → B … 25 → Z, 26 → AA. */
    internal fun columna(indice: Int): String {
        var n = indice
        val sb = StringBuilder()
        do {
            sb.insert(0, ('A' + n % 26))
            n = n / 26 - 1
        } while (n >= 0)
        return sb.toString()
    }

    internal fun escapar(texto: String): String = texto
        .replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        // Caracteres de control que XML 1.0 no admite.
        .filter { it == '\n' || it == '\r' || it == '\t' || it >= ' ' }

    private const val CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/></Types>"""
    private const val RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""
    private const val WORKBOOK = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Cobros" sheetId="1" r:id="rId1"/></sheets></workbook>"""
    private const val WORKBOOK_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>"""
    private const val STYLES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts><fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/><xf numFmtId="4" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/></cellXfs></styleSheet>"""
}
