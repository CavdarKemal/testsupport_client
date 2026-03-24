package de.creditreform.crefoteam.cte.tesun.util.replacer;

public class ReplacementMapping {
    private Long toBeReplacedCrefo;
    private Long targetCrefo;
    private Integer eignerVC;
    private String tagNameEignerVC;

    public ReplacementMapping(Long toBeReplacedCrefo) {
        this.toBeReplacedCrefo = toBeReplacedCrefo;
    }

    public Long getToBeReplacedCrefo() {
        return toBeReplacedCrefo;
    }

    public Long getTargetCrefo() {
        return targetCrefo;
    }

    public Integer getEignerVC() {
        return eignerVC;
    }

    public String getTagNameEignerVC() {
        return tagNameEignerVC;
    }

    public void setToBeReplacedCrefo(Long toBeReplacedCrefo) {
        this.toBeReplacedCrefo = toBeReplacedCrefo;
    }

    public void setTargetCrefo(Long targetCrefo) {
        this.targetCrefo = targetCrefo;
    }

    public void setEignerVC(Integer eignerVC) {
        this.eignerVC = eignerVC;
    }

    public void setTagNameEignerVC(String tagNameEignerVC) {
        this.tagNameEignerVC = tagNameEignerVC;
    }
}
