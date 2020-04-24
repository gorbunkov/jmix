package io.jmix.samples.rest.entity.driver;

import io.jmix.core.metamodel.annotations.InstanceName;

import javax.persistence.*;

@Entity(name = "ref$ExtDriver")
public class ExtDriver extends Driver {

    private static final long serialVersionUID = 5271478633053259678L;

    @Column(name = "INFO", length = 50)
    protected String info;

    // the field is so large that we don't want it to be loaded automatically
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "NOTES")
    @Lob
    protected String notes;

    @InstanceName(relatedProperties = {"name", "info"})
    public String getCaption() {
        return String.format("%s:(%s)", getName(), getInfo());
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
