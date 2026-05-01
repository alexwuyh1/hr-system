let modalInstance = null;

function createModalElement() {
  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.innerHTML = `
    <div class="modal-container">
      <div class="modal-header">
        <h3 class="modal-title"></h3>
        <button class="modal-close">&times;</button>
      </div>
      <div class="modal-body"></div>
      <div class="modal-footer">
        <button class="ghost modal-cancel">取消</button>
        <button class="modal-submit">确定</button>
      </div>
    </div>
  `;
  return overlay;
}

function openModal(title, contentHTML, onSubmit, options = {}) {
  if (modalInstance) {
    closeModal();
  }

  const modal = createModalElement();
  modal.querySelector('.modal-title').textContent = title;
  modal.querySelector('.modal-body').innerHTML = contentHTML;

  if (options.wide) {
    modal.querySelector('.modal-container').style.maxWidth = '800px';
  }

  const closeBtn = modal.querySelector('.modal-close');
  const cancelBtn = modal.querySelector('.modal-cancel');
  const submitBtn = modal.querySelector('.modal-submit');

  if (options.submitText) {
    submitBtn.textContent = options.submitText;
  }

  function handleClose() {
    closeModal();
  }

  async function handleSubmit() {
    const form = modal.querySelector('form');
    if (form && !form.checkValidity()) {
      form.reportValidity();
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = '提交中...';

    try {
      const formData = form ? Object.fromEntries(new FormData(form)) : {};
      await onSubmit(formData);
      closeModal();
    } catch (err) {
      alert(`操作失败: ${err.message}`);
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = options.submitText || '确定';
    }
  }

  closeBtn.addEventListener('click', handleClose);
  cancelBtn.addEventListener('click', handleClose);
  submitBtn.addEventListener('click', handleSubmit);

  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      handleClose();
    }
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modalInstance === modal) {
      handleClose();
    }
  });

  document.body.appendChild(modal);
  modalInstance = modal;

  requestAnimationFrame(() => {
    modal.classList.add('active');
  });

  const firstInput = modal.querySelector('input, select');
  if (firstInput) {
    firstInput.focus();
  }
}

function closeModal() {
  if (modalInstance) {
    modalInstance.classList.remove('active');
    setTimeout(() => {
      if (modalInstance && modalInstance.parentNode) {
        modalInstance.parentNode.removeChild(modalInstance);
      }
      modalInstance = null;
    }, 200);
  }
}

function showConfirm(message, onConfirm) {
  if (modalInstance) {
    closeModal();
  }

  const modal = createModalElement();
  modal.classList.add('confirm-modal');
  modal.querySelector('.modal-title').textContent = '确认';
  modal.querySelector('.modal-body').textContent = message;
  modal.querySelector('.modal-footer').innerHTML = `
    <button class="ghost modal-cancel">取消</button>
    <button class="modal-confirm" style="background: #dc2626;">确定</button>
  `;

  const cancelBtn = modal.querySelector('.modal-cancel');
  const confirmBtn = modal.querySelector('.modal-confirm');

  function handleClose() {
    closeModal();
  }

  async function handleConfirm() {
    confirmBtn.disabled = true;
    confirmBtn.textContent = '处理中...';
    try {
      await onConfirm();
      closeModal();
    } catch (err) {
      alert(`操作失败: ${err.message}`);
      confirmBtn.disabled = false;
      confirmBtn.textContent = '确定';
    }
  }

  cancelBtn.addEventListener('click', handleClose);
  confirmBtn.addEventListener('click', handleConfirm);

  modal.addEventListener('click', (e) => {
    if (e.target === modal) {
      handleClose();
    }
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modalInstance === modal) {
      handleClose();
    }
  });

  document.body.appendChild(modal);
  modalInstance = modal;

  requestAnimationFrame(() => {
    modal.classList.add('active');
  });
}

window.openModal = openModal;
window.closeModal = closeModal;
window.showConfirm = showConfirm;
