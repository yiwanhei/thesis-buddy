package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.entity.ThesisApplication;

import java.util.List;

@Mapper
public interface ApplicationDao {
    
    ThesisApplication selectByStudentId(int studentId);
    
    ThesisApplication selectById(int applicationId);
    
    int insert(ThesisApplication application);
    
    int deleteById(int applicationId);
    
    List<ThesisApplication> selectPendingByTeacher(int teacherId);
    
    int updateStatus(AuditDTO auditDTO);
    
    List<ThesisApplication> selectHistoryByTeacher(int teacherId);
    
    int countApprovedByTopic(@Param("topicId") Integer topicId);
    
    int countReservedByTopic(@Param("topicId") Integer topicId);
    
    List<ThesisApplication> selectReservedByTopic(@Param("topicId") Integer topicId);
    
    int batchResetReserved();
    
    int releaseReservedSlots(@Param("topicId") Integer topicId, @Param("remark") String remark);
    
    // 释放指定队伍的预占名额（保留第一个，释放其余的）
    int releaseTeamReservedSlots(@Param("teamId") Integer teamId, @Param("topicId") Integer topicId, @Param("remark") String remark);
}
