package com.example.baoxiao.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 制度问答结果。
 */
public class ConsultResponse {

    private String answer;
    private LocalDate validOn;
    private int matchedChunks;
    private List<Source> sources = new ArrayList<>();

    public ConsultResponse() {
    }

    public ConsultResponse(String answer, LocalDate validOn, int matchedChunks, List<Source> sources) {
        this.answer = answer;
        this.validOn = validOn;
        this.matchedChunks = matchedChunks;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public LocalDate getValidOn() {
        return validOn;
    }

    public void setValidOn(LocalDate validOn) {
        this.validOn = validOn;
    }

    public int getMatchedChunks() {
        return matchedChunks;
    }

    public void setMatchedChunks(int matchedChunks) {
        this.matchedChunks = matchedChunks;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    /** 命中的制度来源，便于员工追溯与核对。 */
    public static class Source {

        private Long policyId;
        private String code;
        private String title;
        private int version;
        private String category;

        public Source() {
        }

        public Source(Long policyId, String code, String title, int version, String category) {
            this.policyId = policyId;
            this.code = code;
            this.title = title;
            this.version = version;
            this.category = category;
        }

        public Long getPolicyId() {
            return policyId;
        }

        public void setPolicyId(Long policyId) {
            this.policyId = policyId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }
    }
}
