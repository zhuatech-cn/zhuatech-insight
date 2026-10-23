/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.insight.workbench;

import static cn.zhuatech.insight.workbench.WorkbenchInput.*;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@Service
public class AiWorkbenchService {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  public Map<String, Object> execute(Map<String, Object> r) {
    validate(r);

    keys(r, "segments");
    Set<String> ids = new HashSet<>();
    BigDecimal baseline = BigDecimal.ZERO, current = BigDecimal.ZERO, absolute = BigDecimal.ZERO;
    List<Map<String, Object>> parsed = new ArrayList<>();
    for (var s : rows(r, "segments", 1, 300)) {
      keys(s, "id", "baseline", "current");
      String id = id(s, "id");
      unique(ids, id);
      var a = decimal(s, "baseline", -1e12, 1e12);
      var b = decimal(s, "current", -1e12, 1e12);
      var delta = b.subtract(a);
      baseline = baseline.add(a);
      current = current.add(b);
      absolute = absolute.add(delta.abs());
      parsed.add(Map.of("id", id, "baseline", a, "current", b, "delta", delta));
    }
    var totalDelta = current.subtract(baseline);
    List<Map<String, Object>> out = new ArrayList<>();
    for (var s : parsed) {
      var delta = (BigDecimal) s.get("delta");
      out.add(
          Map.of(
              "id",
              s.get("id"),
              "delta",
              delta,
              "netShareDefined",
              totalDelta.signum() != 0,
              "netContributionPercent",
              totalDelta.signum() == 0
                  ? BigDecimal.ZERO
                  : delta
                      .multiply(new BigDecimal("100"))
                      .divide(totalDelta, 6, RoundingMode.HALF_UP),
              "absoluteShareDefined",
              absolute.signum() > 0,
              "absoluteMovementPercent",
              absolute.signum() == 0
                  ? BigDecimal.ZERO
                  : delta
                      .abs()
                      .multiply(new BigDecimal("100"))
                      .divide(absolute, 6, RoundingMode.HALF_UP)));
    }
    return Map.of(
        "baselineTotal",
        baseline,
        "currentTotal",
        current,
        "netChange",
        totalDelta,
        "absoluteMovement",
        absolute,
        "segments",
        out);
  }
}
