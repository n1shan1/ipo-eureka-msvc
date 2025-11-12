package com.ipo.app.entity;

import java.util.Objects;
import java.util.Set;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "allotments")
public class Allotment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipoId;

    @ElementCollection
    private Set<String> winnerApplicationIds;

    @ElementCollection
    private Set<String> nonWinnerApplicationIds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Allotment allotment = (Allotment) o;
        return Objects.equals(id, allotment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Allotment{" +
                "id=" + id +
                ", ipoId='" + ipoId + '\'' +
                ", winnerApplicationIds=" + winnerApplicationIds +
                ", nonWinnerApplicationIds=" + nonWinnerApplicationIds +
                '}';
    }
}