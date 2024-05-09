package com.grinder.service;

import com.grinder.domain.dto.MemberDTO;
import com.grinder.domain.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import static com.grinder.domain.dto.MemberDTO.*;

public interface MemberService {

    Member findMemberById(String memberId);
    MemberDTO.FindMemberAndImageDTO findMemberAndImageById(String memberId);

    MemberDTO.FindMemberAndImageDTO findMemberAndImageByEmail(String email);

    void updateMemberRole(String memberId);

    boolean deleteMember(String memberId);

    boolean recoverMember(String memberId);

    Slice<FindMemberDTO> searchMemberSlice(String role, String nickname, Pageable pageable);

    Member findMemberByEmail(String email);

    public boolean addMember(MemberRequestDto request);
}
