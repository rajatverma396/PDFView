package com.github.barteksc.pdfviewer;


import com.github.barteksc.pdfviewer.model.SearchRecord;
import com.shockwave.pdfium.PdfiumCore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PDocSearchTask implements Runnable {
    private final ArrayList<SearchRecord> arr = new ArrayList<>();

    private final WeakReference<PDFView> pdoc;
    public final AtomicBoolean abort = new AtomicBoolean();
    public final String key;
    private Thread t;
    public final int flag = 0;
    private long keyStr;

    private boolean finished;

    public PDocSearchTask(PDFView pdoc, String key) {

        this.pdoc = new WeakReference<>(pdoc);
        this.key = key + "\0";
    }

    public long getKeyStr( ) {
        if(keyStr==0) {
            keyStr = PdfiumCore.nativeGetStringChars(key);
        }
        return keyStr;
    }

    @Override
    public void run() {
        PDFView a = this.pdoc.get();
        if (a == null) {
            return;
        }
        if (finished) {
            //a.setSearchResults(arr);
            //a.showT("findAllTest_Time : "+(System.currentTimeMillis()-CMN.ststrt)+" sz="+arr.size());
            a.endSearch();
        } else {


            SearchRecord schRecord;
            for (int i = 0; i < a.getPageCount(); i++) {
                if (abort.get()) {
                    break;
                }
                schRecord =a.findPageCached(key, i, 0);

                if (schRecord != null) {
                    a.notifyItemAdded(this, arr, schRecord, i);
                } else {
                  //  a.notifyProgress(i);
                }
            }

            finished = true;
            t = null;
            a.post(this);
        }
    }

    public void start() {
        if (finished) {
            return;
        }
        if (t == null) {
            PDFView a = this.pdoc.get();
            if (a != null) {
                a.startSearch(arr, key, flag);
            }
            t = new Thread(this);
            t.start();
        }
    }

    public void abort() {
        abort.set(true);
    }

    public boolean isAborted() {
        return abort.get();
    }
}
