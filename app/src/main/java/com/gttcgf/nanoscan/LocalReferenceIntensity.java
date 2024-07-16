package com.gttcgf.nanoscan;

import com.ISCSDK.ISCNIRScanSDK;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocalReferenceIntensity implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Date upDateTime;
    private List<Integer> referenceIntensity = new ArrayList<>();
    private final ISCNIRScanSDK.ScanConfiguration referenceInformation;

    public LocalReferenceIntensity(ISCNIRScanSDK.ScanConfiguration referenceInformation, List<Integer> referenceIntensity, Date upDateTime) {
        this.referenceInformation = referenceInformation;
        this.referenceIntensity = referenceIntensity;
        this.upDateTime = upDateTime;
    }

    public boolean isEmpty() {
        return referenceIntensity.isEmpty();
    }

    public List<Integer> getReferenceIntensity() {
        return referenceIntensity;
    }

    public Date getUpDateTime() {
        return upDateTime;
    }

    public ISCNIRScanSDK.ScanConfiguration getReferenceInformation() {
        return referenceInformation;
    }
}
