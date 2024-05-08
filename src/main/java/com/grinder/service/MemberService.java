package com.grinder.service;

import com.grinder.domain.dto.MemberDTO;
import com.grinder.domain.entity.Member;
import org.springframework.data.domain.Pageable;

import java.util.List;
import static com.grinder.domain.dto.MemberDTO.*;

public interface MemberService {

    Member findMemberById(String memberId);
    MemberDTO.FindMemberAndImageDTO findMemberAndImageById(String memberId);

    MemberDTO.FindMemberAndImageDTO findMemberAndImageByEmail(String email);

    void updateMemberRole(String memberId);

    void updateMemberIsDeleted(String memberId);

    List<FindMemberDTO> searchMemberSlice(String role, String nickname, Pageable pageable);

    Member findMemberByEmail(String email);

    public boolean addMember(MemberRequestDto request);

    boolean checkEmail(String email);
    boolean checkNickname(String nickname);

    boolean sendCodeToEmail(String toEmail);

    boolean verifiedCode(String email, String authCode);

    boolean changePassword(String email);

}
