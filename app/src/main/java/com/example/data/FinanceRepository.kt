package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val allTransactions: Flow<List<TransactionEntity>> = financeDao.getAllTransactions()
    val allBills: Flow<List<BillEntity>> = financeDao.getAllBills()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        financeDao.deleteTransactionById(id)
    }

    suspend fun insertBill(bill: BillEntity) {
        financeDao.insertBill(bill)
    }

    suspend fun updateBill(bill: BillEntity) {
        financeDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: BillEntity) {
        financeDao.deleteBill(bill)
    }

    suspend fun deleteBillById(id: Int) {
        financeDao.deleteBillById(id)
    }

    suspend fun deleteAllTransactions() {
        financeDao.deleteAllTransactions()
    }

    suspend fun deleteAllBills() {
        financeDao.deleteAllBills()
    }
}
