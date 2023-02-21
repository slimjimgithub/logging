package com.anz.rtl.transactions.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.anz.rtl.transactions.request.TransactionRequestParam;
import com.anz.rtl.transactions.request.TransactionsRequest;
import com.anz.rtl.transactions.response.Error;
import com.anz.rtl.transactions.response.ErrorsResponse;
import com.anz.rtl.transactions.response.HandleMethodArgumentNotValid;

@RestControllerAdvice
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(CustomizedResponseEntityExceptionHandler.class);
	
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ErrorsResponse> handleAllExceptions(Exception ex, WebRequest request) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		ErrorsResponse exceptionResponse = new ErrorsResponse(new Error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error", request.getDescription(false), date));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(ValidationException.class)
	public final ResponseEntity<ErrorsResponse> handleDateValidationExceptions(Exception ex, WebRequest request) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		ErrorsResponse exceptionResponse = new ErrorsResponse(
				new Error(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), request.getDescription(false), date));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(ServiceFailedException.class)
	public final ResponseEntity<ErrorsResponse> handleServiceFailedExceptions(Exception ex, WebRequest request) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		ErrorsResponse exceptionResponse = new ErrorsResponse(
				new Error(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage(), request.getDescription(false), date));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.UNPROCESSABLE_ENTITY);
	}

	@Override
	@ExceptionHandler(HandleMethodArgumentNotValid.class)
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		BindingResult result = ex.getBindingResult();
		List<FieldError> fieldErrors = result.getFieldErrors();
		ErrorsResponse exceptionResponse = new ErrorsResponse(new Error(HttpStatus.BAD_REQUEST.value(),
				"Bad Request - Request validation failed", fieldErrors.get(0).getDefaultMessage(), date));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	protected ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex,
			HttpServletRequest request) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		// BindingResult result = ex.getMessage();
		// List<FieldError> fieldErrors = result.getFieldErrors();
		String[] message = ex.getMessage().split(":");
		ErrorsResponse exceptionResponse;
		if(message.length>0) {
			exceptionResponse = new ErrorsResponse(new Error(HttpStatus.BAD_REQUEST.value(),
					"Bad Request - Request validation failed", message[1], date));
		}
		exceptionResponse = new ErrorsResponse(new Error(HttpStatus.BAD_REQUEST.value(),
				"Bad Request - Request validation failed", message[1], date));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);

	}

	@ExceptionHandler(SessionExpiredException.class)
	public final ResponseEntity<ErrorsResponse> SessionExpiredExceptions(Exception ex, WebRequest req) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		ErrorsResponse exceptionResponse = new ErrorsResponse(
				new Error(HttpStatus.REQUEST_TIMEOUT.value(), ex.getMessage(), "Session Time Out", date));
		
		return new ResponseEntity<>(exceptionResponse, HttpStatus.REQUEST_TIMEOUT);
	}
	
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Object> handleControllerException(MethodArgumentTypeMismatchException ex, WebRequest req) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		ErrorsResponse exceptionResponse = new ErrorsResponse(new Error(HttpStatus.BAD_REQUEST.value(),
				"Bad Request - Request validation failed ", "Validation failed for "+ ex.getName() + ". Passed value is " + ex.getValue(),
				date));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<Object> handleMissingRequestHeaderException(MissingRequestHeaderException ex, WebRequest req) {
		LOG.error("ERROR:", ex);
		ZonedDateTime date= LocalDateTime.now().atZone(ZoneId.of("Australia/Melbourne"));
		ErrorsResponse exceptionResponse = new ErrorsResponse(new Error(HttpStatus.BAD_REQUEST.value(),
				"Bad Request - Request validation failed ", "Missing request header parameter "+ex.getHeaderName(),
				 date));
		return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
	}
}
