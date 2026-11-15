"""
Configuração da conexão com o banco de dados SQLite via SQLAlchemy.
"""
from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

# O arquivo soundmatch.db é criado automaticamente na pasta onde a API é executada.
DATABASE_URL = "sqlite:///./soundmatch.db"

# check_same_thread=False: necessário no SQLite porque o FastAPI pode
# atender requisições em threads diferentes.
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})

# Fábrica de sessões: cada requisição abre uma sessão própria com o banco.
SessionLocal = sessionmaker(bind=engine, autocommit=False, autoflush=False)


class Base(DeclarativeBase):
    """Classe base da qual todos os modelos (tabelas) herdam."""
    pass


def get_db():
    """
    Dependência do FastAPI: entrega uma sessão do banco para o endpoint
    e garante que ela seja fechada ao final da requisição.
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
