package com.carpooling.common.privacy;

import com.carpooling.common.dto.UserSummaryDTO;

/**
 * Minimizacao de dados pessoais (RGPD, art. 5.o, n.o 1, alinea c).
 *
 * <p>Listagens administrativas nao precisam do email e telefone completos para
 * moderacao; expomos apenas o suficiente para identificar o registo.</p>
 */
public final class PiiMasker {

    private PiiMasker() {
    }

    /** {@code joao.silva@example.com} -> {@code j********a@example.com} */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.length() <= 2) {
            return local.charAt(0) + "***" + domain;
        }
        return local.charAt(0) + "*".repeat(local.length() - 2) + local.charAt(local.length() - 1) + domain;
    }

    /** {@code +351912345678} -> {@code +351******678}: mantem indicativo e ultimos 3 digitos. */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        String compact = phone.replaceAll("\\s+", "");
        if (compact.length() <= 4) {
            return "***";
        }
        int keepStart = compact.startsWith("+") ? Math.min(4, compact.length() - 3) : 0;
        int keepEnd = 3;
        int masked = compact.length() - keepStart - keepEnd;
        if (masked <= 0) {
            return "***";
        }
        return compact.substring(0, keepStart) + "*".repeat(masked) + compact.substring(compact.length() - keepEnd);
    }

    /** Copia do DTO com email e telefone mascarados (o original nao e alterado). */
    public static UserSummaryDTO mask(UserSummaryDTO user) {
        if (user == null) {
            return null;
        }
        return UserSummaryDTO.builder()
                .id(user.getId())
                .email(maskEmail(user.getEmail()))
                .fullName(user.getFullName())
                .phoneNumber(maskPhone(user.getPhoneNumber()))
                .role(user.getRole())
                .rating(user.getRating())
                .ratingCount(user.getRatingCount())
                .isBlocked(user.getIsBlocked())
                .build();
    }
}
