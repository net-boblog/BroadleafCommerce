/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.payment.service;

import java.util.Date;

import javax.annotation.Resource;

import org.broadleafcommerce.payment.domain.PaymentInfo;
import org.broadleafcommerce.payment.domain.PaymentLog;
import org.broadleafcommerce.payment.domain.PaymentResponseItem;
import org.broadleafcommerce.payment.service.exception.PaymentException;
import org.broadleafcommerce.payment.service.exception.PaymentProcessorException;
import org.broadleafcommerce.payment.service.module.PaymentModule;
import org.broadleafcommerce.payment.service.type.PaymentInfoType;
import org.broadleafcommerce.payment.service.type.PaymentLogEventType;
import org.broadleafcommerce.payment.service.type.TransactionType;
import org.springframework.stereotype.Service;

@Service("blPaymentService")
public class PaymentServiceImpl implements PaymentService {

    protected PaymentModule paymentModule;

    @Resource
    protected PaymentInfoService paymentInfoService;

    public PaymentModule getPaymentModule() {
        return paymentModule;
    }

    public void setPaymentModule(PaymentModule paymentModule) {
        this.paymentModule = paymentModule;
    }

    public PaymentResponseItem authorize(PaymentContext paymentContext) throws PaymentException  {
        logPaymentStartEvent(paymentContext, TransactionType.AUTHORIZE);
        PaymentResponseItem response = null;
        PaymentException paymentException = null;
        try {
            response = paymentModule.authorize(paymentContext);
        } catch (PaymentException e) {
            if (e instanceof PaymentProcessorException) {
                response = ((PaymentProcessorException) e).getPaymentResponseItem();
            }
            paymentException = e;
            throw e;
        } finally {
            logResponseItem(paymentContext, response, TransactionType.AUTHORIZE);
            logPaymentFinishEvent(paymentContext, TransactionType.AUTHORIZE, paymentException);
        }

        return response;
    }

    public PaymentResponseItem authorizeAndDebit(PaymentContext paymentContext) throws PaymentException {
        logPaymentStartEvent(paymentContext, TransactionType.AUTHORIZEANDDEBIT);
        PaymentResponseItem response = null;
        PaymentException paymentException = null;
        try {
            response = paymentModule.authorizeAndDebit(paymentContext);
        } catch (PaymentException e) {
            if (e instanceof PaymentProcessorException) {
                response = ((PaymentProcessorException) e).getPaymentResponseItem();
            }
            paymentException = e;
            throw e;
        } finally {
            logResponseItem(paymentContext, response, TransactionType.AUTHORIZEANDDEBIT);
            logPaymentFinishEvent(paymentContext, TransactionType.AUTHORIZEANDDEBIT, paymentException);
        }

        return response;
    }

    public PaymentResponseItem balance(PaymentContext paymentContext) throws PaymentException {
        logPaymentStartEvent(paymentContext, TransactionType.BALANCE);
        PaymentResponseItem response = null;
        PaymentException paymentException = null;
        try {
            response = paymentModule.balance(paymentContext);
        } catch (PaymentException e) {
            if (e instanceof PaymentProcessorException) {
                response = ((PaymentProcessorException) e).getPaymentResponseItem();
            }
            paymentException = e;
            throw e;
        } finally {
            logResponseItem(paymentContext, response, TransactionType.BALANCE);
            logPaymentFinishEvent(paymentContext, TransactionType.BALANCE, paymentException);
        }

        return response;
    }

    public PaymentResponseItem credit(PaymentContext paymentContext) throws PaymentException {
        logPaymentStartEvent(paymentContext, TransactionType.CREDIT);
        PaymentResponseItem response = null;
        PaymentException paymentException = null;
        try {
            response = paymentModule.credit(paymentContext);
        } catch (PaymentException e) {
            if (e instanceof PaymentProcessorException) {
                response = ((PaymentProcessorException) e).getPaymentResponseItem();
            }
            paymentException = e;
            throw e;
        } finally {
            logResponseItem(paymentContext, response, TransactionType.CREDIT);
            logPaymentFinishEvent(paymentContext, TransactionType.CREDIT, paymentException);
        }

        return response;
    }

    public PaymentResponseItem debit(PaymentContext paymentContext) throws PaymentException {
        logPaymentStartEvent(paymentContext, TransactionType.DEBIT);
        PaymentResponseItem response = null;
        PaymentException paymentException = null;
        try {
            response = paymentModule.debit(paymentContext);
        } catch (PaymentException e) {
            if (e instanceof PaymentProcessorException) {
                response = ((PaymentProcessorException) e).getPaymentResponseItem();
            }
            paymentException = e;
            throw e;
        } finally {
            logResponseItem(paymentContext, response, TransactionType.DEBIT);
            logPaymentFinishEvent(paymentContext, TransactionType.DEBIT, paymentException);
        }

        return response;
    }

    public PaymentResponseItem voidPayment(PaymentContext paymentContext) throws PaymentException {
        logPaymentStartEvent(paymentContext, TransactionType.VOIDPAYMENT);
        PaymentResponseItem response = null;
        PaymentException paymentException = null;
        try {
            response = paymentModule.voidPayment(paymentContext);
        } catch (PaymentException e) {
            if (e instanceof PaymentProcessorException) {
                response = ((PaymentProcessorException) e).getPaymentResponseItem();
            }
            paymentException = e;
            throw e;
        } finally {
            logResponseItem(paymentContext, response, TransactionType.VOIDPAYMENT);
            logPaymentFinishEvent(paymentContext, TransactionType.VOIDPAYMENT, paymentException);
        }

        return response;
    }

    @Override
    public Boolean isValidCandidate(PaymentInfoType paymentType) {
        return paymentModule.isValidCandidate(paymentType);
    }

    protected void logResponseItem(PaymentContext paymentContext, PaymentResponseItem response, TransactionType transactionType) {
        if (response != null) {
            response.setTransactionType(transactionType);
            response.setUserName(paymentContext.getUserName());
            PaymentInfo info = paymentContext.getPaymentInfo();
            if (info != null) {
                response.setPaymentInfo(info);
                response.setCustomer(info.getOrder().getCustomer());
                response.setPaymentInfoReferenceNumber(info.getReferenceNumber());
                info.getPaymentResponseItems().add(response);
                paymentInfoService.save(info);
            } else {
                paymentInfoService.save(response);
            }
        }
    }

    protected void logPaymentStartEvent(PaymentContext paymentContext, TransactionType transactionType) {
        PaymentLog log = paymentInfoService.createLog();
        log.setLogType(PaymentLogEventType.START);
        log.setTransactionTimestamp(new Date());
        log.setTransactionSuccess(Boolean.TRUE);
        log.setTransactionType(transactionType);
        log.setUserName(paymentContext.getUserName());
        log.setExceptionMessage(null);

        PaymentInfo info = paymentContext.getPaymentInfo();
        if (info != null) {
            log.setCustomer(info.getOrder().getCustomer());
            log.setPaymentInfoReferenceNumber(info.getReferenceNumber());
            log.setAmountPaid(info.getAmount());
            log.setPaymentInfo(info);
            info.getPaymentLogs().add(log);
            paymentInfoService.save(info);
        } else {
            paymentInfoService.save(log);
        }
    }

    protected void logPaymentFinishEvent(PaymentContext paymentContext, TransactionType transactionType, Exception e) {
        PaymentLog log = paymentInfoService.createLog();
        log.setLogType(PaymentLogEventType.FINISHED);
        log.setTransactionTimestamp(new Date());
        log.setTransactionSuccess(e==null?Boolean.TRUE:Boolean.FALSE);
        log.setTransactionType(transactionType);
        log.setUserName(paymentContext.getUserName());
        String exceptionMessage;
        if (e != null) {
            exceptionMessage = e.getMessage();
            if (exceptionMessage != null) {
                if (exceptionMessage.length() >= 255) {
                    exceptionMessage = exceptionMessage.substring(0, 254);
                }
            } else {
                exceptionMessage = e.getClass().getName();
            }
        } else {
            exceptionMessage = null;
        }
        log.setExceptionMessage(exceptionMessage);

        PaymentInfo info = paymentContext.getPaymentInfo();
        if (info != null) {
            log.setCustomer(info.getOrder().getCustomer());
            log.setPaymentInfoReferenceNumber(info.getReferenceNumber());
            log.setAmountPaid(info.getAmount());
            log.setPaymentInfo(info);
            info.getPaymentLogs().add(log);
            paymentInfoService.save(info);
        } else {
            paymentInfoService.save(log);
        }
    }
}
