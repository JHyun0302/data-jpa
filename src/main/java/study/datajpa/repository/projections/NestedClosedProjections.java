package study.datajpa.repository.projections;

/**
 * Projection : 중첩 구조 처리
 */
public interface NestedClosedProjections {
    String getUsername(); // 최적화 가능

    TeamInfo getTeam(); // 최적화 불가능

    interface TeamInfo {
        String getName();
    }
}
