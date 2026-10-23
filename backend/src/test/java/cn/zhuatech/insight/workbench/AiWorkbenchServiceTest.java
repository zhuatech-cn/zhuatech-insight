/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.insight.workbench;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
class AiWorkbenchServiceTest {
  private final AiWorkbenchService service = new AiWorkbenchService();

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  private Map<String, Object> sample() {
    return Map.<String, Object>ofEntries(
        Map.entry(
            "segments",
            List.of(
                Map.<String, Object>ofEntries(
                    Map.entry("id", "A"), Map.entry("baseline", 100), Map.entry("current", 130)),
                Map.<String, Object>ofEntries(
                    Map.entry("id", "B"), Map.entry("baseline", 200), Map.entry("current", 200)))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void computesBusinessResult() {
    var result = service.execute(sample());
    assertEquals(0, ((BigDecimal) result.get("netChange")).compareTo(new BigDecimal("30")));
    assertEquals(
        new BigDecimal("100.000000"),
        ((Map<?, ?>) ((List<?>) result.get("segments")).get(0)).get("netContributionPercent"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void coversFeatureBoundary() {
    var invalid = new HashMap<>(sample());
    invalid.put("segments", List.of());
    assertThrows(IllegalArgumentException.class, () -> service.execute(invalid));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void coversAdditionalBoundaryOne() {
    var r =
        service.execute(
            Map.of(
                "segments",
                List.of(
                    Map.of("id", "A", "baseline", 10, "current", 20),
                    Map.of("id", "B", "baseline", 20, "current", 10))));
    assertEquals(false, ((Map<?, ?>) ((List<?>) r.get("segments")).get(0)).get("netShareDefined"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void coversAdditionalBoundaryTwo() {
    var r =
        service.execute(
            Map.of("segments", List.of(Map.of("id", "A", "baseline", 10, "current", 10))));
    assertEquals(
        false, ((Map<?, ?>) ((List<?>) r.get("segments")).get(0)).get("absoluteShareDefined"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsExcessiveNumericScale() {
    assertThrows(
        IllegalArgumentException.class,
        () -> WorkbenchInput.decimal(Map.of("v", new BigDecimal("1e-1000000")), "v", 0, 1));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            WorkbenchInput.decimal(
                Map.of("v", new BigDecimal("0.1234567890123456789012345678901")), "v", 0, 1));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void deterministicAndDoesNotMutateInput() {
    var input = sample();
    String before = input.toString();
    assertEquals(service.execute(input), service.execute(input));
    assertEquals(before, input.toString());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsNull() {
    assertThrows(IllegalArgumentException.class, () -> service.execute(null));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsMissingFields() {
    assertThrows(IllegalArgumentException.class, () -> service.execute(Map.of()));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsUnknownFields() {
    var input = new HashMap<>(sample());
    input.put("__unknown", true);
    assertThrows(IllegalArgumentException.class, () -> service.execute(input));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsOversizedText() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.execute(Map.of("payload", "x".repeat(64001))));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsDeepInput() {
    Map<String, Object> v = Map.of("x", 1);
    for (int i = 0; i < 14; i++) v = Map.of("x", v);
    final var input = v;
    assertThrows(IllegalArgumentException.class, () -> service.execute(input));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void httpRequiresAuthentication() throws Exception {
    var mvc = MockMvcBuilders.standaloneSetup(new AiWorkbenchController(service)).build();
    mvc.perform(
            post("/api/workbench/variance-contribution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"segments\":[{\"id\":\"A\",\"baseline\":100,\"current\":130},{\"id\":\"B\",\"baseline\":200,\"current\":200}]}"))
        .andExpect(status().isUnauthorized());
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void httpAllowsAuthenticatedComputation() throws Exception {
    var mvc = MockMvcBuilders.standaloneSetup(new AiWorkbenchController(service)).build();
    mvc.perform(
            post("/api/workbench/variance-contribution")
                .principal(new TestingAuthenticationToken("test-user", "unused", "ROLE_OPERATOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"segments\":[{\"id\":\"A\",\"baseline\":100,\"current\":130},{\"id\":\"B\",\"baseline\":200,\"current\":200}]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void httpRejectsInvalidInput() throws Exception {
    var mvc = MockMvcBuilders.standaloneSetup(new AiWorkbenchController(service)).build();
    mvc.perform(
            post("/api/workbench/variance-contribution")
                .principal(new TestingAuthenticationToken("test-user", "unused", "ROLE_OPERATOR"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsAnonymousAuthenticationObject() {
    var actor =
        new AnonymousAuthenticationToken(
            "test-key",
            "anonymous",
            List.of(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    assertEquals(
        401, new AiWorkbenchController(service).run(sample(), actor).getStatusCode().value());
  }
}
