package org.openmrs.module.laboratorymanagement.web.controller2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.laboratorymanagement.LabOrderParent;
import org.openmrs.module.laboratorymanagement.OrderObs;
import org.openmrs.module.laboratorymanagement.advice.LabTestConstants;
import org.openmrs.module.laboratorymanagement.utils.GlobalPropertiesMgt;
import org.openmrs.module.laboratorymanagement.utils.LabUtils;
import org.openmrs.module.mohappointment.model.MoHAppointment;
import org.openmrs.module.mohappointment.utils.AppointmentUtil;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The main controller.
 */
@Controller
public class LabOrdersMainController {
	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(value = "/module/laboratorymanagement/portlets/labOrderPortlet.portlet", method = RequestMethod.GET)
	public void getDrugOrderPortlet(ModelMap model, HttpServletRequest request) {
		// categories of lab tests on lab request form

		// get all selected Lab tests and save them as Order
		Map<String, String[]> parameterMap = request.getParameterMap();

		String patientIdstr = request.getParameter("patientId");
		Patient patient = Context.getPatientService().getPatient(Integer.parseInt(patientIdstr));

		/**
		 * <<<<<<< Appointment Consultation waiting list management >>>>>>>
		 */
		MoHAppointment appointment = null;
		// This is from the Provider's Appointment dashboard
		if (request.getParameter("appointmentId") != null) {
			appointment = AppointmentUtil
					.getWaitingAppointmentById(Integer.parseInt(request.getParameter("appointmentId")));
			LabUtils.setConsultationAppointmentAsAttended(appointment);
		}

		/**
		 * <<<<<<<<< APPOINTMENTS STUFF ENDS HERE >>>>>>>>
		 */
		initialiseModelEtc(model, patientIdstr, patient);

		if (request.getParameter("orderId") != null) {
			LabUtils.cancelLabOrder(request.getParameter("orderId"));

			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "The Lab order is successfully cancelled");
		}
		// request.getRequestDispatcher("patientDashboard.form?patientId=" +
		// patientIdstr).forward(request, response);
	}

	private void initialiseModelEtc(ModelMap model, String patientIdstr, Patient patient) {
		// request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
		// "Orders created");
		List<Concept> conceptCategories = GlobalPropertiesMgt.getLabExamCategories();
		List<LabOrderParent> lopList = LabUtils.getsLabOrdersByCategories(conceptCategories);
		int hematologyId = LabTestConstants.hematologyId;
		int parasitologyId = LabTestConstants.PARASITOLOGYID;
		int hemostasisId = LabTestConstants.hemostasisId;
		int bacteriologyId = LabTestConstants.bacteriologyId;
		int spermConceptId = LabTestConstants.spermConceptId;
		int urinaryChemistryId = LabTestConstants.urineChemistryId;
		int immunoSerologyId = LabTestConstants.immunoSerologyId;
		int bloodChemistryId = LabTestConstants.bloodChemistryId;
		int toxicologyId = LabTestConstants.toxicologyId;
		Location dftLoc = Context.getLocationService().getDefaultLocation();

		User user = Context.getAuthenticatedUser();
		String providerName = user.getFamilyName() + " " + user.getGivenName();
		String patientName = patient.getFamilyName() + " " + patient.getMiddleName() + " " + patient.getGivenName();

		// String orderTypeIdStr =
		// Context.getAdministrationService().getGlobalProperty("orderType.labOrderTypeId");
		Map<ConceptName, Collection<Concept>> mappedLabOrder = new HashMap<ConceptName, Collection<Concept>>();
		mappedLabOrder = LabUtils.getLabExamsToOrder(Integer.parseInt(patientIdstr));
		OrderService orderService = Context.getOrderService();
		// get observations by Person
		List<Order> orders = orderService.getOrders(patient, orderService.getCareSettingByName("Inpatient"),
				Context.getOrderService().getOrderTypeByName("Lab test"), true);

		Map<Date, List<OrderObs>> orderObsMap = LabUtils.getMappedOrderToObs(orders, patient);

		// Map<String, Object> orderObsMapOrdered = new TreeMap<String,
		// Object>((Comparator<? super String>) orderObsMap);

		// int spermConceptId = LabTestConstants.SPERMCONCEPTID;

		model.put("dftLoc", dftLoc);
		// model.put("locations", locations);
		model.put("patientId", patientIdstr);
		model.put("mappedLabOrder", mappedLabOrder);
		model.put("obsMap", orderObsMap);
		model.put("providerName", providerName);
		model.put("patienName", patientName);
		model.put("labOrderparList", lopList);
		model.put("hematology", Context.getConceptService().getConcept(hematologyId).getName().getName());
		model.put("parasitology", Context.getConceptService().getConcept(parasitologyId).getName().getName());
		model.put("hemostasis", Context.getConceptService().getConcept(hemostasisId).getName().getName());
		model.put("bacteriology", Context.getConceptService().getConcept(bacteriologyId).getName().getName());
		model.put("spermogram", Context.getConceptService().getConcept(spermConceptId).getName().getName());
		model.put("urinaryChemistry", Context.getConceptService().getConcept(urinaryChemistryId).getName().getName());
		model.put("immunoSerology", Context.getConceptService().getConcept(immunoSerologyId).getName().getName());
		model.put("bloodChemistry", Context.getConceptService().getConcept(bloodChemistryId).getName().getName());
		model.put("toxicology", Context.getConceptService().getConcept(toxicologyId).getName().getName());
	}

	@RequestMapping(value = "/module/laboratorymanagement/portlets/labOrderPortlet.portlet", method = RequestMethod.POST)
	public void postDrugOrderPortlet(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		// Saving selected lab orders:

		// get all selected Lab tests and save them as Order
		Map<String, String[]> parameterMap = request.getParameterMap();

		String patientIdstr = request.getParameter("patientId");
		Patient patient = Context.getPatientService().getPatient(Integer.parseInt(patientIdstr));
		String orderTypeIdStr = Context.getAdministrationService()
				.getGlobalProperty("laboratorymanagement.orderType.labOrderTypeId");
		OrderType orderType = StringUtils.isNotBlank(orderTypeIdStr)
				? Context.getOrderService().getOrderType(Integer.parseInt(orderTypeIdStr)) : null;

		Encounter enc = createEncounter(new Date(), Context.getAuthenticatedUser(),
				Context.getLocationService().getDefaultLocation(), patient, new ArrayList<Obs>());
		if (enc != null) {
			enc = Context.getEncounterService().saveEncounter(enc);
		}
		LabUtils.saveSelectedLabOrders(parameterMap, patient, enc, orderType);
		model.put("msg", "The Lab order is successfully created");

		LabUtils.createWaitingLabAppointment(patient, enc);

		initialiseModelEtc(model, patientIdstr, patient);

		try {
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "The Lab order is successfully created");
			response.sendRedirect(request.getContextPath() + "/patientDashboard.form?patientId=" + patientIdstr);
		} catch (IOException e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, e.getMessage());
		}
	}

	private Encounter createEncounter(Date encounterDate, User provider, Location location, Patient patient,
			List<Obs> obsList) {
		Encounter enc = new Encounter();

		if (encounterDate.after(new Date()))
			encounterDate = new Date();
		try {
			enc.setDateCreated(new Date());
			enc.setEncounterDatetime(encounterDate);
			enc.setProvider(provider);
			enc.setLocation(location);
			enc.setPatient(patient);
			enc.setEncounterType(Context.getEncounterService().getEncounterType(2));// ADULT_RETURN

			for (Obs o : obsList) {
				if (null != o)
					enc.addObs(o);
				else
					System.out.println("An observation has not been saved because it was null.");
			}
		} catch (Exception e) {
			System.out.println("An Error occured when trying to create an encounter :\n");
			e.printStackTrace();
			enc = null;
		}
		return enc;
	}
}
