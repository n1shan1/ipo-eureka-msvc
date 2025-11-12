package com.ipo.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllotmentDoneEvent implements Serializable {
    private String ipoId;
    private Set<String> winnerApplicationIds;
    private Set<String> nonWinnerApplicationIds;

    public String getIpoId() {
        return ipoId;
    }

    public void setIpoId(String ipoId) {
        this.ipoId = ipoId;
    }

    public Set<String> getWinnerApplicationIds() {
        return winnerApplicationIds;
    }

    public void setWinnerApplicationIds(Set<String> winnerApplicationIds) {
        this.winnerApplicationIds = winnerApplicationIds;
    }

    public Set<String> getNonWinnerApplicationIds() {
        return nonWinnerApplicationIds;
    }

    public void setNonWinnerApplicationIds(Set<String> nonWinnerApplicationIds) {
        this.nonWinnerApplicationIds = nonWinnerApplicationIds;
    }
}