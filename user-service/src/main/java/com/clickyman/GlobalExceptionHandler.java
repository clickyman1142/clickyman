package com.clickyman;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.clickyman.constant.SystemCode;
import com.clickyman.dto.ExceptionDetails;
import com.clickyman.dto.PropertyError;
import com.clickyman.dto.RequestValidateError;
import com.clickyman.exception.ClickymanException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	@Value("${app.debug}")
	private boolean debugMode;
	
	@ExceptionHandler(value = { ClickymanException.class })
	protected ResponseEntity<ExceptionDetails> handleException(ClickymanException ex, WebRequest request) {
		ExceptionDetails exceptionDetails = ExceptionDetails.builder()
			.message(ex.getMessage())
			.debugMessage(debugMode ? ex.getDebugMessage() : null)
			.systemCode(ex.getSystemCode())
			.dateTime(LocalDateTime.now())
			.status(ex.getStatus() != null ? ex.getStatus() : HttpStatus.INTERNAL_SERVER_ERROR)
			.build();
		
		return new ResponseEntity<ExceptionDetails>(exceptionDetails, exceptionDetails.getStatus());
	}
	
	/**
	 * Handler @Valid response
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		RequestValidateError responseError = RequestValidateError.builder()
			.dateTime(LocalDateTime.now())
			.systemCode(SystemCode.BAD_REQUEST)
			.status(HttpStatus.BAD_REQUEST)
			.propertyErrors(ex.getBindingResult().getFieldErrors().stream().map(error -> {
				return PropertyError.builder().propertyName(error.getField()).messageCode(error.getDefaultMessage()).build();
			}).collect(Collectors.toList()))
			.build();
		return new ResponseEntity<>(responseError, responseError.getStatus());
	}
	
	/**
	 * Just for production mode
	 * @param ex
	 * @param request
	 */
//	@ExceptionHandler(value = { Exception.class })
//	protected ResponseEntity<ExceptionDetails> handleAllException(Exception ex, WebRequest request) {
//		ExceptionDetails exception = ExceptionDetails.builder()
//			.status(HttpStatus.INTERNAL_SERVER_ERROR)
//			.systemCode(SystemCode.UNEXPECTED_ERROR)
//			.dateTime(LocalDateTime.now()).build();
//		return new ResponseEntity<ExceptionDetails>(exception, HttpStatus.INTERNAL_SERVER_ERROR);
//	}
}
