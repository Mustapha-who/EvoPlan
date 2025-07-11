package evoplan.services.Partner;

import evoplan.entities.Partnership;
import evoplan.entities.Partner;
import evoplan.services.Partner.PartnershipService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PartnershipReminderScheduler {
    private static PartnershipReminderScheduler instance;
    private final ScheduledExecutorService scheduler;
    private final PartnershipService partnershipService;
    private final EmailSender emailSender;
    private final GoogleCalendar googleCalendarService;
    private boolean running = false;


    private PartnershipReminderScheduler(PartnershipService partnershipService, EmailSender emailSender, GoogleCalendar googleCalendarService) {
        this.partnershipService = partnershipService;
        this.emailSender = emailSender;
        this.googleCalendarService = googleCalendarService;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public static synchronized PartnershipReminderScheduler getInstance(
            PartnershipService partnershipService,
            EmailSender emailSender,
            GoogleCalendar googleCalendarService) {
        if (instance == null) {
            instance = new PartnershipReminderScheduler(partnershipService, emailSender, googleCalendarService);
        }
        return instance;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndSendReminders();
            } catch (Exception e) {
                System.err.println("❌ Scheduler encountered an error: " + e.getMessage());
                e.printStackTrace();
                // Restart the scheduler
                stop();
                start();
            }
        }, 0, 1, TimeUnit.HOURS);
    }
    public synchronized boolean isRunning() {
        return running;
    }
    public void stop() {
        if (!scheduler.isShutdown()) {
            System.out.println("🛑 Shutting down scheduler...");
            scheduler.shutdown(); // Graceful shutdown
            try {
                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                    System.out.println("⚠️ Scheduler did not terminate, forcing shutdown!");
                    scheduler.shutdownNow(); // Force shutdown
                }
            } catch (InterruptedException e) {
                System.err.println("❌ Interrupted while shutting down scheduler!");
                scheduler.shutdownNow();
            }
        }
    }

    private void checkAndSendReminders() {
        try {
            System.out.println("🔍 Checking for partnerships nearing their end date...");

            List<Partnership> partnerships = partnershipService.getAll();
            LocalDate today = LocalDate.now();

            System.out.println("Total partnerships: " + partnerships.size()); // Debug log

            for (Partnership partnership : partnerships) {
                LocalDate endDate = LocalDate.parse(partnership.getDate_fin(), DateTimeFormatter.ISO_LOCAL_DATE);

                System.out.println("Partnership ID: " + partnership.getId_partnership() +
                        ", End Date: " + endDate); // Debug log

                // Check if the partnership is ending in 7 days
                if (endDate.isAfter(today) && endDate.isBefore(today.plusDays(8))) {
                    System.out.println("Partnership ID: " + partnership.getId_partnership() +
                            " is ending in 7 days."); // Debug log

                    Partner partner = partnershipService.getPartnerById(partnership.getId_partner());

                    if (partner != null) {
                        System.out.println("Sending reminder email to partner: " + partner.getEmail()); // Debug log
                        sendReminderEmail(partner, partnership);

                        System.out.println("Creating calendar event for partnership: " + partnership.getId_partnership()); // Debug log
                        createCalendarEvent(partnership);
                    } else {
                        System.err.println("❌ Partner not found for partnership ID: " + partnership.getId_partnership());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in checkAndSendReminders: " + e.getMessage());
            e.printStackTrace();
        }

    }


    private void sendReminderEmail(Partner partner, Partnership partnership) {
        String subject = "Partnership Ending Soon";
        String body = String.format(
                "Dear Partner ,\n\n" +
                        "Your partnership with us is ending on %s. Would you like to extend it?\n\n" +
                        "Best regards,\n" +
                        "Evoplan Team",
                 partnership.getDate_fin()
        );

        System.out.println("Sending email to: " + partner.getEmail()); // Debug log
        System.out.println("Email subject: " + subject); // Debug log
        System.out.println("Email body: " + body); // Debug log

        emailSender.sendEmail(partner.getEmail(), subject, body);
        System.out.println("📧 Reminder email sent to " + partner.getEmail());
    }


    private void createCalendarEvent(Partnership partnership) {
        try {
            Partner partner = partnershipService.getPartnerById(partnership.getId_partner());
            if (partner == null) {
                System.err.println("❌ Partner not found for partnership ID: " + partnership.getId_partnership());
                return;
            }

            // Get or create a calendar for the partner
            String partnerCalendarId = googleCalendarService.getOrCreateCalendar(partner.getEmail());
            if (partnerCalendarId == null) {
                System.err.println("❌ Failed to get or create calendar for partner: " + partner.getEmail());
                return;
            }

            // Share the calendar with the partner
            googleCalendarService.shareCalendarWithPartner(partnerCalendarId, partner.getEmail());

            LocalDate startDate = LocalDate.parse(partnership.getDate_debut(), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate endDate = LocalDate.parse(partnership.getDate_fin(), DateTimeFormatter.ISO_LOCAL_DATE);

            // Create a unique event summary that includes the partnership ID for better uniqueness
            String eventSummary = "Partnership Ending: Make sure to contact us if you want to Extend :) :p :! " ;

            // Check if the event already exists with improved logging
            System.out.println("🔍 Checking if event exists for partnership ID: " + partnership.getId_partnership());
            if (googleCalendarService.eventExists(partnerCalendarId, eventSummary, startDate, endDate)) {
                System.out.println("⚠️ Event already exists for partnership ID: " + partnership.getId_partnership() + " - Skipping creation");
                return;
            }

            System.out.println("📅 Creating new calendar event for partnership ID: " + partnership.getId_partnership());
            System.out.println("📧 Partner Email: " + partner.getEmail());
            System.out.println("🗓️ Partner Calendar ID: " + partnerCalendarId);

            // Create the event in the partner's calendar and invite them
            googleCalendarService.createEvent(
                    partnerCalendarId,
                    eventSummary,
                    "Partnership with " + partner.getLogo(),
                    startDate,
                    endDate,
                    partner.getEmail()
            );
            System.out.println("✅ Google Calendar event created for partnership ID: " + partnership.getId_partnership());
        } catch (Exception e) {
            System.err.println("❌ Failed to create Google Calendar event for partnership ID: " + partnership.getId_partnership());
            e.printStackTrace();
        }
    }
}