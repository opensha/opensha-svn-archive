package com.jrefinery.data;

import java.util.Date;

public class TimeAllocation {

    protected Date start;

    protected Date end;

    public TimeAllocation(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public Date getStart() {
        return this.start;
    }

    public Date getEnd() {
        return this.end;
    }

}