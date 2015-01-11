package com.eerojaaskelainen.ostosbudjetti.databaseHelpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.eerojaaskelainen.ostosbudjetti.models.Ostosrivi;
import com.eerojaaskelainen.ostosbudjetti.models.Tuote;

/**
 * Created by Eero on 3.1.2015.
 */
public class OstosriviHelper {

    public static Cursor haeOstosrivitCursor(SQLiteDatabase readableDatabase, String[] projection, String selection, String[] selectionArgs, String sortOrder, String koriID, String riviID) {
        SQLiteQueryBuilder kysely = new SQLiteQueryBuilder();
        kysely.setTables(Ostosrivi.TABLE_NAME + " NATURAL JOIN " + Tuote.TABLE_NAME);

        String limit = null;

        if (koriID != null) {
            kysely.appendWhere(Ostosrivi.FULL_OSTOSKORI + "=" + koriID);
        }
        if (riviID != null) {
            kysely.appendWhere(Ostosrivi.FULL_ID + "=" + riviID);
            kysely.setDistinct(true);
            limit = "1";
        }

        return kysely.query(
                readableDatabase,
                lisaaRivisummaProjectioon(projection),
                selection,
                selectionArgs,
                null,
                null,
                sortOrder,
                limit
        );
    }

    /**
     * Otetaan projectioon mukaan rivisumman muodostus
     * @param projection    Content Providerin lähettämä projektio (jos on)
     * @return  Palauttaa projection missä mukana rivisumma
     */
    private static String[] lisaaRivisummaProjectioon(String[] projection) {
        String[] vieProjection;

        if (projection == null) {
            // Tyhjä projection, eli kaikki kentät halutaan:
            vieProjection = new String[] {
                    Ostosrivi.OSTOSKORI,
                    Ostosrivi._ID,
                    Tuote.EAN,
                    Tuote.NIMI,
                    Ostosrivi.A_HINTA,
                    Ostosrivi.LKM,
                    Ostosrivi.RIVISUMMA_GENERATOR
            };
        }
        else {
            // Projektiossa oli mukana kenttiä:
            vieProjection = new String[projection.length+1];
            for (int i =0; i < projection.length; i++)
            {
                vieProjection[i] = projection[i];
            }
            vieProjection[projection.length] = Ostosrivi.RIVISUMMA_GENERATOR;
        }
        return vieProjection;
    }

    public static long luoOstosrivi(SQLiteDatabase writableDatabase, long ostoskori_id, long tuote_id, double ahinta, int lkm) {
        // Tutki onko ostoskori olemassa:
        if (!OstoskoriHelper.onkoOstoskoria(writableDatabase,ostoskori_id)) {
            throw new IllegalArgumentException("There is no basket with the ID "+ ostoskori_id);
        }
        // Tutki onko sama tuote olemassa samassa listassa:
        Cursor lista = haeOstosrivitCursor(writableDatabase,
                new String[]{Ostosrivi.FULL_ID,Ostosrivi.FULL_A_HINTA,Ostosrivi.FULL_LKM},
                Ostosrivi.TUOTE + "=" + tuote_id,
                null,
                null,Long.toString(ostoskori_id),null);
        if (lista.getCount()>0) {
            lista.moveToFirst();
            // Sama tuote on jo olemassa. Tarkista ja tee mahdolliset päivitykset:
            paivitaOstosrivi(writableDatabase,
                    lista.getLong(lista.getColumnIndex(Ostosrivi._ID)),
                    ahinta, lkm);

            return lista.getLong(lista.getColumnIndex(Ostosrivi._ID));
        }

        // Samaa tuotetta ei ole, joten luodaan uusi rivi:
        // Tutki onko tuote olemassa:
        if (!TuoteHelper.onkoTuotetta(writableDatabase,tuote_id)) {
            throw new UnsupportedOperationException("There is no product with ID "+ tuote_id);
        }

        // Kaikki OK: Tehdään lisäys:
        ContentValues cV = new ContentValues();
        cV.put(Ostosrivi.OSTOSKORI,ostoskori_id);
        cV.put(Ostosrivi.TUOTE,tuote_id);
        cV.put(Ostosrivi.A_HINTA,ahinta);
        cV.put(Ostosrivi.LKM,lkm);

        return writableDatabase.insert(Ostosrivi.TABLE_NAME,null,cV);
    }

    private static void paivitaOstosrivi(SQLiteDatabase writableDatabase, long ostosrivi_id, double ahinta, int lkm) {
        //TODO: Tee ostosrivin päivitykset!
        throw new UnsupportedOperationException("Ostosrivin päivitystä ei oo tehty viel");
    }
}
