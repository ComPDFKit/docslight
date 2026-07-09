import pytest


def test_cli_help_exits_zero_and_prints_name(capsys: pytest.CaptureFixture[str]) -> None:
    from docslight.cli import main

    with pytest.raises(SystemExit) as exc_info:
        main(["--help"])

    assert exc_info.value.code == 0
    output = capsys.readouterr().out
    assert "docslight" in output
    assert "parse" in output
    assert "extract" in output
    assert "web" in output
