package com.investmentsentinel.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.investmentsentinel.config.AppProperties;
import com.investmentsentinel.domain.HistoricalInvestment;
import com.investmentsentinel.domain.Investment;
import com.investmentsentinel.domain.MarketReference;
import com.investmentsentinel.domain.NotificationRecord;
import com.investmentsentinel.domain.TriggerEvent;
import com.investmentsentinel.repository.HistoricalInvestmentRepository;
import com.investmentsentinel.repository.InvestmentRepository;
import com.investmentsentinel.repository.MarketReferenceRepository;
import com.investmentsentinel.repository.NotificationRepository;
import com.investmentsentinel.repository.TriggerEventRepository;
import com.investmentsentinel.service.market.MarketDataService;
import com.investmentsentinel.service.market.MarketDataSnapshot;
import com.investmentsentinel.service.monitoring.DrawdownCalculation;
import com.investmentsentinel.service.monitoring.InvestmentMonitoringService;
import com.investmentsentinel.service.sms.SmsResponse;
import com.investmentsentinel.service.sms.SmsService;
import com.investmentsentinel.web.dto.InvestmentRecordDto;
import com.investmentsentinel.web.dto.ReferenceResetDto;

@Controller
public class DashboardController {

    private final InvestmentRepository investmentRepository;
    private final MarketReferenceRepository marketReferenceRepository;
    private final HistoricalInvestmentRepository historicalInvestmentRepository;
    private final TriggerEventRepository triggerEventRepository;
    private final NotificationRepository notificationRepository;
    private final MarketDataService marketDataService;
    private final InvestmentMonitoringService monitoringService;
    private final SmsService smsService;
    private final AppProperties properties;

    public DashboardController(InvestmentRepository investmentRepository,
                               MarketReferenceRepository marketReferenceRepository,
                               HistoricalInvestmentRepository historicalInvestmentRepository,
                               TriggerEventRepository triggerEventRepository,
                               NotificationRepository notificationRepository,
                               MarketDataService marketDataService,
                               InvestmentMonitoringService monitoringService,
                               SmsService smsService,
                               AppProperties properties) {
        this.investmentRepository = investmentRepository;
        this.marketReferenceRepository = marketReferenceRepository;
        this.historicalInvestmentRepository = historicalInvestmentRepository;
        this.triggerEventRepository = triggerEventRepository;
        this.notificationRepository = notificationRepository;
        this.marketDataService = marketDataService;
        this.monitoringService = monitoringService;
        this.smsService = smsService;
        this.properties = properties;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Investment> investments = investmentRepository.findByActiveTrue();
        Map<Long, MarketReference> references = new HashMap<>();
        Map<Long, MarketDataSnapshot> liveMarket = new HashMap<>();
        Map<Long, DrawdownCalculation> calculations = new HashMap<>();

        for (Investment inv : investments) {
            marketReferenceRepository.findFirstByInvestmentIdOrderByCreatedAtDesc(inv.getId())
                    .ifPresent(ref -> {
                        references.put(inv.getId(), ref);

                        MarketDataSnapshot snapshot = inv.getName().contains("ATLAS")
                                ? marketDataService.getSp500Level().orElse(null)
                                : marketDataService.getIciciGoldEtfPrice().orElse(null);

                        if (snapshot != null) {
                            liveMarket.put(inv.getId(), snapshot);
                            AppProperties.TierConfig tier = inv.getName().contains("ATLAS")
                                    ? properties.getInvestments().getAtlas()
                                    : properties.getInvestments().getGold();

                            DrawdownCalculation calc = DrawdownCalculation.calculate(
                                    snapshot.value(),
                                    ref.getReferenceValue(),
                                    tier.getNormalAmount(),
                                    tier.getDrawdown1(), tier.getAmount1(),
                                    tier.getDrawdown2(), tier.getAmount2(),
                                    tier.getDrawdown3(), tier.getAmount3()
                            );
                            calculations.put(inv.getId(), calc);
                        }
                    });
        }

        model.addAttribute("investments", investments);
        model.addAttribute("references", references);
        model.addAttribute("liveMarket", liveMarket);
        model.addAttribute("calculations", calculations);
        model.addAttribute("recentNotifications", notificationRepository.findTop50ByOrderBySentAtDesc());
        model.addAttribute("recentTriggers", triggerEventRepository.findTop50ByOrderByTriggeredAtDesc());
        model.addAttribute("historicalInvestments", historicalInvestmentRepository.findTop50ByOrderByInvestmentDateDesc());
        model.addAttribute("appProperties", properties);

        return "dashboard";
    }

    @PostMapping("/investments/record")
    public String recordInvestment(@ModelAttribute InvestmentRecordDto dto, RedirectAttributes ra) {
        try {
            monitoringService.recordInvestment(dto.investmentId(), dto.amount(), dto.investmentDate(), dto.notes());
            ra.addFlashAttribute("successMessage", "Investment recorded successfully. Next review date updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/references/reset")
    public String resetReference(@ModelAttribute ReferenceResetDto dto, RedirectAttributes ra) {
        try {
            monitoringService.resetReference(dto.investmentId(), dto.newReferenceValue(), dto.referenceDate(), dto.reason());
            ra.addFlashAttribute("successMessage", "Reference reset successfully recorded in audit log.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/monitoring/trigger")
    public String triggerMonitoring(RedirectAttributes ra) {
        try {
            monitoringService.runDailyEvaluation();
            ra.addFlashAttribute("successMessage", "Daily evaluation executed successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Monitoring evaluation failed: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/sms/test")
    public String sendTestSms(@RequestParam(required = false) String message, RedirectAttributes ra) {
        try {
            SmsResponse response = smsService.sendTestSms(message);
            if (response.success()) {
                ra.addFlashAttribute("successMessage", "Test SMS dispatched successfully via " + response.providerName() + "!");
            } else {
                ra.addFlashAttribute("errorMessage", "Test SMS failed via " + response.providerName() + ": " + response.errorMessage());
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "SMS failure: " + e.getMessage());
        }
        return "redirect:/";
    }

    // JSON REST API endpoints
    @GetMapping("/api/status")
    @ResponseBody
    public Map<String, Object> getStatusApi() {
        return Map.of(
                "status", "RUNNING",
                "timezone", properties.getTimezone(),
                "monitoringCron", properties.getMonitoring().getCron(),
                "smsProvider", properties.getSms().getProvider(),
                "earlyDrawdownAlerts", properties.getMonitoring().isEarlyDrawdownAlerts()
        );
    }
}
