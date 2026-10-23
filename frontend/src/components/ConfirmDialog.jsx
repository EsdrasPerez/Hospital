import { Alert, Button, Modal, ModalBody, ModalFooter, ModalHeader } from 'reactstrap'

/** Diálogo modal de confirmación (p. ej. para desactivar un registro). */
export default function ConfirmDialog({
  abierto,
  titulo,
  mensaje,
  textoConfirmar = 'Confirmar',
  textoCancelar = 'Cancelar',
  peligro = false,
  cargando = false,
  error,
  onConfirmar,
  onCancelar,
}) {
  return (
    <Modal isOpen={Boolean(abierto)} toggle={cargando ? undefined : onCancelar} centered backdrop={cargando ? 'static' : true} fade={false}>
      <ModalHeader closeAriaLabel="Cerrar" toggle={cargando ? undefined : onCancelar}>{titulo}</ModalHeader>
      <ModalBody>
        <p className="mb-0">{mensaje}</p>
        {error && (
          <Alert color="danger" fade={false} className="mt-3 mb-0" role="alert">
            {error}
          </Alert>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" outline onClick={onCancelar} disabled={cargando}>
          {textoCancelar}
        </Button>
        <Button color={peligro ? 'danger' : 'primary'} onClick={onConfirmar} disabled={cargando}>
          {cargando ? 'Procesando...' : textoConfirmar}
        </Button>
      </ModalFooter>
    </Modal>
  )
}
