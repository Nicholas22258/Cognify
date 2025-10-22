package com.example.cognify;

/*
 * @Author Nicholas Leong        EDUV4551823
 * @Author Aarya Manowah         be.2023.q4t9k6
 * @Author Nyasha Masket        BE.2023.R3M0Y0
 * @Author Sakhile Lesedi Mnisi  BE.2022.j9f3j4
 * @Author Dominic Newton       EDUV4818782
 * @Author Kimberly Sean Sibanda EDUV4818746
 *
 * Supervisor: Stacey Byrne      Stacey.byrne@eduvos.com
 * */

import java.util.Date;

public class StudyMaterial {
    private String materialId;
    private String title;
    private String uploadedBy;
    private String uploadedByUserId;
    private Date uploadDate;
    private String subject;
    private String fileUrl;
    private String fileSize;
    private String fileType;
    private String status; // "pending", "approved", "rejected"
    private int downloadCount;
    private String rejectionReason;

    // Empty constructor required for Firestore
    public StudyMaterial() {
    }

    // Constructor with parameters
    public StudyMaterial(String materialId, String title, String uploadedBy, String uploadedByUserId,
                         Date uploadDate, String subject, String fileUrl, String fileSize,
                         String fileType, String status, int downloadCount, String rejectionReason) {
        this.materialId = materialId;
        this.title = title;
        this.uploadedBy = uploadedBy;
        this.uploadedByUserId = uploadedByUserId;
        this.uploadDate = uploadDate;
        this.subject = subject;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.status = status;
        this.downloadCount = downloadCount;
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(String uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return "StudyMaterial{" +
                "materialId='" + materialId + '\'' +
                ", title='" + title + '\'' +
                ", uploadedBy='" + uploadedBy + '\'' +
                ", uploadedByUserId='" + uploadedByUserId + '\'' +
                ", uploadDate=" + uploadDate +
                ", subject='" + subject + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileSize='" + fileSize + '\'' +
                ", fileType='" + fileType + '\'' +
                ", status='" + status + '\'' +
                ", downloadCount=" + downloadCount +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}