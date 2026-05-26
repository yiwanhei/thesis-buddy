package org.example.thesisbuddy.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.thesisbuddy.dto.teacher.AuditDTO;
import org.example.thesisbuddy.entity.ThesisApplication;

import java.time.LocalDateTime;
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
    
    // 释放指定学生的预占记录
    int releaseStudentReservation(@Param("studentId") Integer studentId, @Param("remark") String remark);
    
    // 释放队伍所有成员的预占记录
    int releaseTeamAllReservations(@Param("teamId") Integer teamId, @Param("remark") String remark);
    
    // 查询学生的预占记录（按学生ID和状态）
    ThesisApplication selectByStudentIdAndStatus(@Param("studentId") Integer studentId, @Param("status") Integer status);
    
    // ========== v2 新增方法 ==========
    
    // 查询某个题目的已占用名额数（预占中1+审核中2+已通过3）
    int countOccupiedByTopic(@Param("topicId") Integer topicId);
    
    // 查询所有超时的预占记录（reserve_until < NOW() AND status=1）
    List<ThesisApplication> selectExpiredReservations();
    
    // 批量释放所有超时预占（reserve_until < NOW() AND status=1 → status=0）
    int batchReleaseExpiredReservations();
    
    // 批量更新队伍下所有预占记录的reserve_until
    int updateReserveUntilByTeam(@Param("teamId") Integer teamId,
                                 @Param("reserveUntil") LocalDateTime reserveUntil);
    
    // 批量更新队伍下指定学生的reserve_until
    int updateReserveUntilByTeamAndStudents(@Param("teamId") Integer teamId,
                                            @Param("studentIds") List<Integer> studentIds,
                                            @Param("reserveUntil") LocalDateTime reserveUntil);
    
    // 查询队伍下所有指定状态的申请记录
    List<ThesisApplication> selectByTeamIdAndStatus(@Param("teamId") Integer teamId,
                                                    @Param("status") Integer status);
    
    // 批量更新队伍下某状态申请为另一状态（限定学生列表）
    int batchUpdateStatusByTeam(@Param("teamId") Integer teamId,
                                @Param("oldStatus") Integer oldStatus,
                                @Param("newStatus") Integer newStatus,
                                @Param("remark") String remark,
                                @Param("studentIds") List<Integer> studentIds);
    
    // 释放队伍中不在指定学生列表中的预占记录
    int releaseExcessByTeam(@Param("teamId") Integer teamId,
                            @Param("studentIds") List<Integer> studentIds,
                            @Param("remark") String remark);
    
    // 将占位记录（student_id=-1）分配给真实学生
    int assignPlaceholder(@Param("teamId") Integer teamId,
                          @Param("newStudentId") Integer newStudentId,
                          @Param("reserveUntil") LocalDateTime reserveUntil);

    // 将真实学生的预占记录转为占位记录（student_id=-1），保留时间和队伍名额不受影响
    int convertToPlaceholder(@Param("teamId") Integer teamId,
                             @Param("studentId") Integer studentId,
                             @Param("remark") String remark);
}
