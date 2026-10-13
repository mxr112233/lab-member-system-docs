package com.nynu.codelab.labmember.member.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberPageVO {

    private List<MemberVO> records;

    private long total;

    private long page;

    private long pageSize;
}
