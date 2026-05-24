package org.example.thesisbuddy.dto.student;

import lombok.Data;

@Data
public class TeamInviteDTO {
    private int inviteId;
    private int inviterId;
    private int inviteeId;
    private int teamId;
    private String status;
}
